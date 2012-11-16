package ads

import scala.collection.mutable.ListBuffer
import scala.util.Random

import java.util.concurrent.TimeoutException

import akka.actor.{ Actor, ActorContext, ActorRef, ActorSystem, Props, TypedActor, TypedProps }
import akka.dispatch.{ Promise, Future, Await }
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

import ads.message._
import ads.schema._
import ads.util._
import ads.Op._

/**
 * Companion object for Transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
object Transaction {

  // random number generator
  private val rand = new Random()

  // transaction id counter
  private var x: Int = 0

  /** 
   * Returns the next available Transaction identifier.
   * 
   * @return The next available TID
   */
  def getNextTID: Int = synchronized {
    val ret = x
    x += 1
    return ret
  } // getNextTID

  def getRandomInt (n: Int): Int = synchronized {
    rand.nextInt(n)
  } // getRandomInt

} // Transaction

/**
 * Interface for a database transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
trait Transaction {

  /**
   * Begin the transaction.
   */
  def begin (): Unit

  /**
   * Read a value into the transaction from the database.
   *
   * This implementation takes advantage of the implicit time provided by akka.
   * If read returns None, then the request timed out.
   *
   * @param oid The object identifier.
   * @return the value of the object as an Option.
   */
  def read (table: String, oid: Int, prop: String): Option[Any]

  /**
   * Write a value into the database.
   *
   * @param oid The object identifier.
   * @param value The value to write into the database.
   */
  def write (table: String, oid: Int, prop: String, value: Any): Unit

  /**
   * Commit this transaction.
   */
  def commit (): Unit

  /**
   * Rollback this transaction.
   */
  def rollback (): Unit

  /**
   * Returns the TID of this transaction
   */
  def getTID (): Option[Int]

  /**
   * Returns the timestamp of this transaction
   */
  def getTimestamp (): Option[Long]

  /**
   * Updates the timestamp of this transaction, returning the new timestamp as
   * an Option.
   *
   * @return The new timestamp as an Option.
   */
  def touch (): Option[Long]

  /**
   * The body of the transaction
   */
  protected def body (): Unit

  /**
   * Excecute this transaction.
   */
  def execute (): Unit = {
    begin
    body
    commit
  } // execute

} // Transaction

/**
 * Implementation of a database transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param m The transaction manager object.
 * @param body The set of statements to be executed by this transaction.
 */
class TransactionImpl (db: Database) extends Transaction {

  // So we can create Promises
  import TypedActor.dispatcher

  implicit val timeout = Timeout(10 seconds)

  /**
   * Used for trace statements
   */
  private lazy val trace = Logging(db.system, TypedActor.context.self) 

  /**
   * The timestamp of the transaction
   */
  private var timestamp = System.currentTimeMillis
      
  /**
   * The transaction identifier.
   */
  private var tid = Transaction.getNextTID

  def deepCopy[A](a: A)(implicit m: reflect.Manifest[A]): A =
    scala.util.Marshal.load[A](scala.util.Marshal.dump(a))

  // implementation of Transaction body()
  def body () {}

  // implementation of Transaction begin()
  def begin () = {
    trace.info("T%d begin".format(tid))
    db.tm ! BeginMessage(this)
  } // begin

  // implementation of Transaction read()
  def read (table: String, oid: Int, prop: String): Option[Any] = {

    trace.info("T%d read(%d)".format(tid, oid))

    var postpone = false
    var ret: ReadResponse = null

    do {
      while (ret == null) {
	try {
	  val future = (db.tm ask ReadMessage(this, table, oid, prop)).mapTo[ReadResponse]
	  ret = Await.result(future, timeout.duration)

	  if (ret.postpone) {
	    trace.info("T%d read(%d) postponed".format(tid, oid))
	    postpone = true
	    Thread sleep 1000
	  } // if

	  if (ret.rollback) this.rollback

	} catch {
	  case e: TimeoutException => trace.warning("T%d read(%d) timed out; trying again".format(tid, oid))
	} // try
      } // while
    } while (postpone)

    trace.info("T%d read(%d) succesfull".format(tid, oid))

    // return the value as an Option
    Some(ret.value)

  } // read

  // implementation of Transaction write()
  def write (table: String, oid: Int, prop: String, value: Any): Unit = {    

    trace.info("T%d write(%d, %s)".format(tid, oid, value))

    var ret: WriteResponse = null
    var postpone = false

    do {
      while (ret == null) {
	try {
	 
	  val future = (db.tm ask WriteMessage(this, table, oid, prop, value)).mapTo[WriteResponse]
	  ret = Await.result(future, timeout.duration).asInstanceOf[WriteResponse]
	 
	  if (ret.postpone) {
	    trace.info("T%d write(%d, %s) postponed".format(tid, oid, value))
	    postpone = true
	    Thread sleep 1000
	  } // if

	  if (ret.rollback) this.rollback

	} catch {
	  case e: TimeoutException => trace.warning("T%d write(%d) timed out; trying again".format(tid, oid))
	} // try
      } // while
    } while (postpone)

  } // write

  // implementation of Transaction commit()
  def commit () = {
    trace.info("T%d commit()".format(tid))
    db.tm ! CommitMessage(this)
    TypedActor(TypedActor.context.system).poisonPill(this)
  } // commit

  // implementation of Transaction rollback()
  def rollback () = {

    trace.warning("T%d rollback()".format(tid))

    this.tid = Transaction.getNextTID
    this.touch

    // make the transaction wait for a random amount of time
    trace.warning("T%d going to sleep for a while".format(tid))
    Thread sleep Transaction.getRandomInt(5000)

//    val child: Transaction = TypedActor(TypedActor.context).typedActorOf(TypedProps(classOf[Transaction], this), "Transaction-%d-child".format(tid))
//    child.execute

  } // rollback

  // implementation of Transaction getTID()
  def getTID (): Option[Int] = Some(tid)

  // implementation of Transaction getTimestamp()
  def getTimestamp (): Option[Long] = Some(timestamp)

  // implementation of Transaction touch()
  def touch (): Option[Long] = {
    trace.info("T%d touch()".format(tid))
    timestamp = System.currentTimeMillis
    getTimestamp
  } // touch

  // implementation of toString
  override def toString = "Transaction-%d".format(tid)

} // Transaction class

object TypedTransactionTestSGC extends App {

  import ads.concurrency.{ SGC, TSO }

  val rand = new Random()

  // set up the database
  val db = new Database("MyTestDB")

  // create a schema
  class PersonSchema () extends Schema ("person", db) {
    register(IntProperty("age"))
    register(StringProperty("name", 32))
  } // PersonSchema

  class StudentSchema () extends Schema ("student", db) {
    register(LongProperty("810"))
    register(StringProperty("name", 32))
  } // StudentSchema

  // register the schema
  db.registerSchema(new PersonSchema())
  db.registerSchema(new StudentSchema())

  for (i <- 1 to 10000) {

    val timpl = new TransactionImpl(db) {
      override def body () {

	val oid  = rand.nextInt(1000)

	val name = read("person", oid, "name").get.asInstanceOf[String]
	val age  = read("person", oid, "age").get.asInstanceOf[Int]
	
	write("person", oid, "name", "bob")
	write("person", oid, "age", age + 1)

        val sName = read("student", oid, "name").get.asInstanceOf[String]

        write("student", oid, "name", name)

      } // body
    } // timpl

    val t: Transaction = db.makeTransaction(timpl, "Transaction-%d".format(i-1))

    //Thread sleep (20 + rand.nextInt(30))
    Thread sleep 50

    // execute the transactiono
    t.execute

  } // for

} // TypedTransactionTestSGC


