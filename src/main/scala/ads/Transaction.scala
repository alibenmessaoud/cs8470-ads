package ads

import scala.collection.mutable.ListBuffer
import scala.util.Random

import akka.actor.{ Actor, ActorContext, ActorRef, ActorSystem, Props, 
                    TypedActor, TypedProps }
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
object TransactionUtil {

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

} // TransactionUtil

/**
 * Interface for a database transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
trait Transaction {

  /**
   * The implicit timeout for blocking messages
   */
  implicit val timeout = Timeout(5 seconds)

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

  /**
   * Used for trace statements
   */
  private val trace = Logging(TypedActor.context.system, TypedActor.context.self) 

  /**
   * The timestamp of the transaction
   */
  private var timestamp = System.currentTimeMillis
      
  /**
   * The transaction identifier.
   */
  private var tid = TransactionUtil.getNextTID
  
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

    val future = ask(tm, ReadMessage(this, oid)).mapTo[Any]
    val ret    = Await.result(future, timeout.duration)

    Some(ret)
  } // read

  // implementation of Transaction write()
  def write (oid: Int, value: Any): Unit = {
    
    trace.info("T%d write(%d, %s)".format(tid, oid, value))
    
    tm ! WriteMessage(this, oid, value) 
    
    // TODO finish implementing

  } // write

  // implementation of Transaction commit()
  def commit () = {
    trace.info("T%d commit()".format(tid))
    tm ! CommitMessage(this)
    exit
  } // commit

  // implementation of Transaction rollback()
  def rollback () = {

    trace.warning("T%d rollback()".format(tid))

    this.tid = TransactionUtil.getNextTID
    this.touch

    // make the transaction wait for a random amount of time
    Thread sleep TransactionUtil.getRandomInt(1000)

    // TODO actually rollback :/

  } // rollback

  // implementation of Transaction getTID()
  def getTID (): Option[Int] = Some(tid)

  // implementation of Transaction getTimestamp()
  def getTimestamp (): Option[Long] = Some(timestamp)

  // implementation of Transaction touch()
  def touch (): Option[Long] = {
    timestamp = System.currentTimeMillis
    getTimestamp
  } // touch


  override def toString = "T%d".format(tid)

} // Transaction class

object TypedTransactionTest extends App {

  val system = ActorSystem("TypedTransactionTest")
  val tm     = system.actorOf(Props[TransactionManager], name = "tm")

  val timpl = new TransactionImpl(tm) {
    override def body () {
      read(7)
      write(7, 32)
    } // body
  } // timpl

  val t: Transaction = TypedActor(system).typedActorOf(TypedProps(classOf[Transaction], timpl), "t")

}
