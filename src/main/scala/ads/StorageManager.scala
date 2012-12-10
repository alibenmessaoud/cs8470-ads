package ads

import scala.collection.mutable.{ HashMap, Map, Set }

import akka.actor.{ Actor, ActorContext, ActorSystem, Props, TypedActor, TypedProps }
import akka.event.{ Logging, LogSource }

import ads.util.FileMap

object StorageManager {

  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = "StorageManager()"
    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

} // StorageManager

case class Read(tid: Int, table: String, oid: Int, prop: String)
case class Write(tid: Int, table: String, oid: Int, prop: String, value: Any)
case class Commit(tid: Int)

class StorageManager (db: Database) extends Actor {

  val trace = Logging(context.system, this) 

  // map from table names to maps of page ids to sets of transaction ids
  private val dirtyMap: Map[String, Map[Int, Set[Int]]] = new HashMap[String, Map[Int, Set[Int]]]()

  // log buffer for recoverability
  private val logBuffer = new LogBuffer(db)

  private def read (table: String, oid: Int, prop: String): Option[Any] = {
    val ret = db.table(table)(oid).get(prop)
    if (ret != null) return Some(ret) else None
  } // read
 
  private def write (table: String, oid: Int, prop: String, value: Any): Option[Any] = { 
    val ret = db.table(table)(oid).set(prop)(value)
    if (ret != null) return Some(ret) else None
  } // write

  private def commit (tid: Int) {
    cleanup(tid)
  } // commit

  private def getPagesMapFor (table: String): Map[Int, Set[Int]] = dirtyMap.get(table) match {

    case Some(map) => return map

    case None => {

      val map = new HashMap[Int, Set[Int]]
      dirtyMap += table -> map
      return map     

    }

  } // getPagesMapFor

  private def getTransactionsForPageOf (table: String, oid: Int): Set[Int] = {

    val pid = db.table(table).getPageIdFor(oid)

    getPagesMapFor(table).get(pid) match {

      case Some(set) => return set

      case None => {
	
	val set = Set.empty[Int]
	getPagesMapFor(table) += pid -> set
	return set

      }

    } // match

  } // getTransactionsForPageOf

  private def makeDirty (tid: Int, table: String, oid: Int): Unit = {
    getTransactionsForPageOf(table, oid).add(tid)
  } // makeDirty

  private def cleanup (tid: Int) {
    for (kv1 <- dirtyMap) {

      // get the table name and the map
      val (table, pmap) = kv1

      for (kv2 <- pmap) {

	// get the page id and the set of transaction for that page
	val (pid, set) = kv2

	if (set.contains(tid)) {

	  // remove this transaction from the set
	  set.remove(tid)

	  // if the set is empty then we can make it clean and flush the table
	  if (set.isEmpty) {
	    trace.info("everything for page %d is committed. let's flush it!".format(pid))
	    db.table(table).fileMap.pages(pid).dirty = false
	    db.table(table).flush
	  } // if

	} // if

      } // for

    } // for
  } // makeClean

  def receive = {

    case Read(tid, table, oid, prop) => {

      trace.info("received read request from %s for Transaction-%d".format(sender, tid))
      logBuffer += new LogEntry(LogType.READ, tid, table, oid, prop)
      logBuffer.persist
      sender ! read(table, oid, prop)

    } // Read

    case Write(tid, table, oid, prop, value) => {

      trace.info("received write request from %s for Transaction-%d".format(sender, tid))
      makeDirty(tid, table, oid)
      logBuffer += new LogEntry(LogType.WRITE, tid, table, oid, prop, value)
      logBuffer.persist
      write(table, oid, prop, value)

    } // Write

    case Commit(tid) => {

      trace.info("received commit notification from %s for Transaction-%d".format(sender, tid))
      logBuffer += new LogEntry(LogType.READ, tid)
      logBuffer.persist
      commit(tid)

    } // Commit

  } // receive

} // StorageManager
