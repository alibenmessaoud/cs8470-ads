package ads

import scala.collection.mutable.{Buffer, ListBuffer}

import java.util.Date
import java.io.RandomAccessFile
import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.Charset

object LogType {
  val READ   = 1  // 2^0
  val WRITE  = 2  // 2^1
  val COMMIT = 4  // 2^2
  val CHK    = 8  // 2^3
  val BEGIN  = 16 // 2^4
} // LogType

object LogEntry {

  val width = 8 + 4 + 4 + 64 + 4 + 64 + 64 + 64

  // consistent encoding
  val encoding = "UTF-8"
  def encoder = Charset.forName(encoding).newEncoder
  def decoder = Charset.forName(encoding).newDecoder

  def fromBytes (bytes: Array[Byte]): LogEntry = {

    // convert from bytes
    val byteBuffer = ByteBuffer.wrap(bytes)
    val timestamp  = byteBuffer.getLong
    val logType    = byteBuffer.getInt
    val tid        = byteBuffer.getInt
    val tableArray = (for (i <- 0 until 64) yield byteBuffer.get()).toArray
    val oid        = byteBuffer.getInt
    val propArray  = (for (i <- 0 until 64) yield byteBuffer.get()).toArray
    val valueArray = (for (i <- 0 until 64) yield byteBuffer.get()).toArray
    val oldArray   = (for (i <- 0 until 64) yield byteBuffer.get()).toArray

    // return the log entry
    val entry = new LogEntry(logType, tid, getStringFromBytes(tableArray), oid, getStringFromBytes(propArray), getStringFromBytes(valueArray), getStringFromBytes(oldArray))
    entry.timestamp = timestamp
    entry

  } // fromBytes

  def getStringFromBytes (bytes: Array[Byte]): String = {

    // convert from bytes
    val byteBuffer = ByteBuffer.wrap(bytes)
    val charBuffer = LogEntry.decoder.decode(byteBuffer)
    val str        = charBuffer.toString
    
    // return the string
    str

  } // getStringFromBytes

  def getStringBytes (str: String): Array[Byte] = {

    // generate a byte array with proper encoding
    val charBuffer = CharBuffer.wrap(str)
    val byteBuffer = LogEntry.encoder.encode(charBuffer)
    val bytes      = byteBuffer.array.slice(byteBuffer.position, byteBuffer.limit)

    // calculate and create the padding
    val padLength = 64 - (bytes.length % 64)
    val padding = Array.ofDim[Byte](padLength)

    // return the padded array
    padding ++ bytes

  } // getStringBytes

} // LogEntry

class LogEntry (val logType: Int, tid: Int, table: String, oid: Int, prop: String, value: Any, oldValue: Any) {

  def this (logType: Int, tid: Int, table: String, oid: Int, prop: String) = this (logType, tid, "", 0, "", "", "")
  def this (logType: Int, tid: Int)  = this (logType, tid, "", 0, "", "", "")

  var timestamp = System.currentTimeMillis
  var persisted = false

  def getTID = tid
  def getTable = table
  def getOID = oid
  def getValue = value
  def getProp = prop
  def getOldValue = oldValue

  def getBytes: Array[Byte] = {

    val tsBytes  = ByteBuffer.allocate(8).putLong(timestamp).array
    val logBytes = ByteBuffer.allocate(4).putInt(logType).array
    val tidBytes = ByteBuffer.allocate(4).putInt(tid).array
    val tblBytes = LogEntry.getStringBytes(table)
    val oidBytes = ByteBuffer.allocate(4).putInt(oid).array
    val prpBytes = LogEntry.getStringBytes(prop)
    val valBytes = LogEntry.getStringBytes(value.toString)
    val oldBytes = LogEntry.getStringBytes(oldValue.toString)
    
    tsBytes ++ logBytes ++ tidBytes ++ tblBytes ++ oidBytes ++ prpBytes ++ valBytes ++ oldBytes
  } // getBytes

  def logTypeString(lt: Int) = lt match {
    case 1 => "Read"
    case 2 => "Write"
    case 4 => "Commit"
    case 8 => "Checkpoint"
  } // logTypeString

  override def toString = "LogEntry(%d, %s, %d, %s, %d, %s, %s)".format(timestamp, logTypeString(logType), tid, table, oid, prop, value)

} // LogEntry

object LogBuffer {

  def recover (db: Database) {

    db.trace.info("Starting recovery protocols...")
    
    // get the logBuffer
    val logBuffer = new LogBuffer(db)

    db.trace.info("Reading up to two full checkpoints...")

    logBuffer.logFile.seek(0)
    var chkCount = 0
    var eofCheck = 0
    do {
      val bytes = Array.ofDim[Byte](LogEntry.width)
      eofCheck = logBuffer.logFile.read(bytes)
      if (eofCheck != -1) {
	val entry = LogEntry.fromBytes(bytes)
	if (entry.logType == LogType.CHK) {
	  chkCount += 1
	} // if
	if (entry.logType == LogType.CHK || entry.logType == LogType.WRITE || entry.logType == LogType.COMMIT) logBuffer += entry
      }
    } while (chkCount < 2 && eofCheck != -1)

    val commits = ListBuffer.empty[Int]

    var fixCount = 0

    if (logBuffer.length > 0) {

      db.trace.info("Database is out of sync with log...")

      // undo all writes
      db.trace.info("Undo phase...")
      
      for (entry <- logBuffer.buffer.reverse) {
	entry.logType match {
	  case 2 => {
 	    db.trace.info("undoing a write = %s".format(entry))
	    // TODO db.table(entry.getTable)(entry.getOID).set(entry.getProp)(entry.getOldValue)
	  }
	  case 4 => {
	    db.trace.info("recording commit")
	    commits += entry.getTID
	  }
	  case 8 => {
	    db.trace.info("recording checkpoint")
	  }
	} // match
      } // for
      
      // redo all committed
      db.trace.info("Redo phase...")      

      for (entry <- logBuffer.iterator if commits.contains(entry.getTID)) {
	entry.logType match {
	  case 2 => {
 	    db.trace.info("redoing a write that committed")
	    // TODO db.table(entry.getTable)(entry.getOID).set(entry.getProp)(entry.getValue)
	    fixCount += 1
	  }
	  case 4 => {}
	  case 8 => {}
	} // match
      } // for

    } // if 

    db.trace.info("Recovery completed with %f%% success!".format(fixCount.toDouble / logBuffer.length.toDouble))
   
  } // recover

} // LogBuffer

class LogBuffer (val db: Database) {

  var logFile = new RandomAccessFile("%s.log".format(db.name), "rw")
  var buffer  = ListBuffer.empty[LogEntry]

  def += (elem: LogEntry) = (buffer += elem)

  def +=: (elem: LogEntry) = (buffer.+=:(elem))

  def length: Int = buffer.length

  def remove (n: Int): LogEntry = buffer remove n

  def insertAll (n: Int, elems: Traversable[LogEntry]): Unit = buffer.insertAll(n, elems)

  def clear(): Unit = buffer.clear

  def update (n: Int, newelem: LogEntry): Unit = buffer.update(n, newelem)

  def apply (n: Int): LogEntry = buffer(n)

  def iterator: Iterator[LogEntry] = buffer.iterator

  def persist: Unit = {

    // go to end of file
    logFile.seek(logFile.length)
    for (entry <- this.iterator if !entry.persisted) {
      logFile.write(entry.getBytes)
      entry.persisted = true
    } // for

    // cleanup
    cleanup()

  } // persist

  def cleanup(): Unit = {

    // remove stuff that's more than 5 checkpoints old

    var count = 0
    var len   = 0

    val newBuffer = ListBuffer.empty[LogEntry]

    for (entry <- buffer.reverse) {
      if (entry.logType == LogType.CHK) count += 1
      if (count <= 5) newBuffer += entry 
    } // for TODO optimize
    
    this.buffer = newBuffer

  } // cleanup

} // LogBuffer






















