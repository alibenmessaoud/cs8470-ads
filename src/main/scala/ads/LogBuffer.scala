package ads

import scala.collection.mutable.{Buffer, ListBuffer}

import java.util.Date
import java.io.RandomAccessFile
import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.Charset

object LogType {
  val READ   = 1 // 2^0
  val WRITE  = 2 // 2^1
  val COMMIT = 4 // 2^2
  val CHK    = 8 // 2^3
} // LogType

class LogEntry (val logType: Int, tid: Int, table: String, oid: Int, prop: String, value: Any) {

  def this (logType: Int, tid: Int, table: String, oid: Int, prop: String) = this (logType, tid, "", 0, "", "")
  def this (logType: Int, tid: Int)  = this (logType, tid, "", 0, "", "")

  val timestamp = System.currentTimeMillis

  var persisted = false

  def getStringBytes(str: String): Array[Byte] = {

    // consistent encoding
    val encoding = "UTF-8"
    def encoder = Charset.forName(encoding).newEncoder
    def decoder = Charset.forName(encoding).newDecoder

    // generate a byte array with proper encoding
    val charBuffer = CharBuffer.wrap(str)
    val byteBuffer = encoder.encode(charBuffer)
    val bytes      = byteBuffer.array.slice(byteBuffer.position, byteBuffer.limit)

    // calculate and create the padding
    val padLength = 64 - (bytes.length % 64)
    val padding = Array.ofDim[Byte](padLength)

    // return the padded array
    padding ++ bytes

  } // getStringBytes

  def getBytes: Array[Byte] = {

    val width = 4 + 4 + 64 + 4 + 512
    val tsBytes  = ByteBuffer.allocate(8).putLong(timestamp).array
    val logBytes = ByteBuffer.allocate(4).putInt(logType).array
    val tidBytes = ByteBuffer.allocate(4).putInt(tid).array
    val tblBytes = getStringBytes(table)
    val oidBytes = ByteBuffer.allocate(4).putInt(oid).array
    val prpBytes = getStringBytes(prop)
    val valBytes = getStringBytes(value.toString)
    
    tsBytes ++ logBytes ++ tidBytes ++ tblBytes ++ oidBytes ++ prpBytes ++ valBytes
  } // getBytes

} // LogEntry


class LogBuffer (db: Database) {

  val logFile = new RandomAccessFile("%s.log".format(db.name), "rw")
  val buffer  = ListBuffer.empty[LogEntry]

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

  } // persist

} // LogBuffer
