package ads

import scala.collection.mutable.{Buffer, ListBuffer}

import java.io.RandomAccessFile

object LogType {
  val READ   = 1
  val WRITE  = 2
  val COMMIT = 4
} // LogType

class LogEntry (val logType: Int) {
  var table: String = ""
  var oid: Int = 0
  var prop: String = ""
  var oldValue: Any = null
  var newValue: Any = null
} // LogEntry

class LogBuffer (db: Database) extends Buffer[LogEntry] {

  val logFile = new RandomAccessFile("%s.log".format(db.name), "rw")
  val buffer  = ListBuffer.empty[LogEntry]

  def += (elem: LogEntry) = (buffer += elem).asInstanceOf[LogBuffer.this.type]
  def +=: (elem: LogEntry) = (buffer.+=:(elem)).asInstanceOf[LogBuffer.this.type]
  def length: Int = buffer.length
  def remove (n: Int): LogEntry = buffer remove n
  def insertAll (n: Int, elems: Traversable[LogEntry]): Unit = buffer.insertAll(n, elems)
  def clear(): Unit = buffer.clear
  def update (n: Int, newelem: LogEntry): Unit = buffer.update(n, newelem)
  def apply (n: Int): LogEntry = buffer(n)
  def iterator: Iterator[LogEntry] = buffer.iterator

} // LogBuffer
