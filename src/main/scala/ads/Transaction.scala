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
import ads.util._
import ads.Op._

/**
 * Companion object for Transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
object Transaction {

  private val rand = new Random()

  /**
   * TID counter
   */
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
  def read (oid: Int): Option[Any]

  /**
   * Write a value into the database.
   *
   * @param oid The object identifier.
   * @param value The value to write into the database.
   */
  def write (oid: Int, value: Any): Unit

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
class TransactionImpl (tm: ActorRef) extends Transaction {

  // So we can create Promises
  import TypedActor.dispatcher

  implicit val timeout = Timeout(10 seconds)

  /**
   * Used for trace statements
   */
  private lazy val trace = Logging(TypedActor.context.system, TypedActor.context.self) 

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
    tm ! BeginMessage(this)
  } // begin

  // implementation of Transaction read()
  def read (oid: Int): Option[Any] = {

    trace.info("T%d read(%d)".format(tid, oid))

    var postpone = false
    var ret: ReadResponse = null

    do {
      while (ret == null) {
	try {
	  val future = (tm ask ReadMessage(this, oid)).mapTo[ReadResponse]
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
  def write (oid: Int, value: Any): Unit = {    
    trace.info("T%d write(%d, %s)".format(tid, oid, value))

    var ret: WriteResponse = null
    var postpone = false

    do {
      while (ret == null) {
	try {
	 
	  val future = (tm ask WriteMessage(this, oid, value)).mapTo[WriteResponse]
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
    tm ! CommitMessage(this)
    TypedActor.context.stop(TypedActor.context.self)
  } // commit

  // implementation of Transaction rollback()
  def rollback () = {

    trace.warning("T%d rollback()".format(tid))

    this.tid = Transaction.getNextTID
    this.touch

    // make the transaction wait for a random amount of time
    trace.warning("T%d going to sleep for a while".format(tid))
    Thread sleep Transaction.getRandomInt(1000)

    // TODO reexecute the body
    // execute   

    // kill the actor
    // TODO TypedActor.context.stop(TypedActor.context.self)

    val child: Transaction = TypedActor(TypedActor.context).typedActorOf(TypedProps(classOf[Transaction], this), "t%d-child".format(tid))
    child.execute

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
  override def toString = "T%d".format(tid)

} // Transaction class

object TypedTransactionTestSGC extends App {

  import ads.concurrency.{ SGC, TSO }

  // Setup the TransactionManager and its ActorSystem
  val tmsys  = ActorSystem("TransactionManager")
  val tm     = tmsys.actorOf(Props{new TransactionManager() with SGC}, name = "tm")

  // Setup the Transaction ActorSystem
  val tsys = ActorSystem("Transaction")

  for (i <- 1 to 10) {

    val timpl = new TransactionImpl(tm) {
      override def body () {
	val a = read(7)
	val c = read(9)
	write(7, 1)
	val b = read(8)
	write(9, 32)
      } // body
    } // timpl

    // The following line turns the TransactionImpl into a TypedActor
    val t: Transaction = TypedActor(tsys).typedActorOf(TypedProps(classOf[Transaction], timpl), "t%d".format(i-1))

    // execute the transaction
    t.execute

  } // for

} // TypedTransactionTestTSO

object TypedTransactionTestTSO extends App {

  import ads.concurrency.{ SGC, TSO }

  // Setup the TransactionManager and its ActorSystem
  val tmsys  = ActorSystem("TransactionManager")
  val tm     = tmsys.actorOf(Props{new TransactionManager() with TSO}, name = "tm")

  // Setup the Transaction ActorSystem
  val tsys = ActorSystem("Transaction")

  for (i <- 1 to 10) {

    val timpl = new TransactionImpl(tm) {
      override def body () {
	val a = read(7)
	val c = read(9)
	write(7, 1)
	val b = read(8)
	write(9, 32)
      } // body
    } // timpl

    // The following line turns the TransactionImpl into a TypedActor
    val t: Transaction = TypedActor(tsys).typedActorOf(TypedProps(classOf[Transaction], timpl), "t%d".format(i-1))

    // execute the transaction
    t.execute

  } // for

} // TypedTransactionTestTSO




