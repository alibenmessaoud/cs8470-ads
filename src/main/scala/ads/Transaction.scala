package ads

import scala.collection.mutable.ListBuffer
import scala.util.Random

import akka.actor.{Actor, ActorRef}
import akka.actor.Props
import akka.event.Logging

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
 * A database transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param m The transaction manager object.
 * @param body The set of statements to be executed by this transaction.
 */
class Transaction (m: ActorRef) extends Actor {

  /**
   * Used for trace statements
   */
  private val trace = Logging(context.system, this) 

  /**
   * The timestamp of the transaction
   */
  var timestamp = System.currentTimeMillis
      
  /**
   * The transaction identifier.
   */
  var tid = Transaction.getNextTID
  
  /**
   * The body of the transaction
   */
  def body() {}

  // implementation of act function for actor
  def act() {
    begin
    body
    commit
  } // act

  /**
   * Begin the transaction.
   */
  def begin() = {
    trace.info("T%d begin".format(tid))
    m ! BeginMessage(self)
  } // begin

  /**
   * Read a value into the transaction from the database.
   *
   * @param oid The object identifier.
   * @return the value of the object.
   */
  def read(oid: Int): Any = {

    trace.info("T%d read(%d)".format(tid, oid))
    var ret: Any = null

    m ! ReadMessage(self, oid)

/*    self.receive {
      case msg: PostponeReadMessage => {
	// wait for 1 sec
	// Thread sleep 1000
//	trace("T%d was instructed to postpone read request".format(tid))
	ret = this.read(oid)
      }
      case value: String => {
//	trace("T%d received value from read request".format(tid))
	ret = value
      }
    } // react
*/
    ret
  } // read

  /**
   * Write a value into the database.
   *
   * @param oid The object identifier.
   * @param value The value to write into the database.
   */
  def write(oid: Int, value: Any): Unit = {
    trace.info("T%d write(%d, %s)".format(tid, oid, value))
    
    m ! WriteMessage(self, oid, value) 
    
/*    self.receive {
      case postpone: PostponeWriteMessage => {
	// wait for 1 sec
	//Thread sleep 1000
	this.write(oid, value)
      } // case
      case _ => { }
    } // react
*/
  } // write


  /**
   * Commit this transaction.
   */
  def commit() = {
    trace.info("T%d commit()".format(tid))
    m ! CommitMessage(self)
    exit
  } // commit

  /**
   * Rollback this transaction.
   */
  def rollback() = {

    trace.info("T%d rollback()".format(tid))

    this.tid = Transaction.getNextTID
    this.timestamp = System.currentTimeMillis

    // make the transaction wait for a random amount of time
    Thread sleep Transaction.getRandomInt(1000)
    
    begin
    body
    commit
    exit

  } // rollback

  override def toString = "T%d".format(tid)

  def receive = {
    case _: TimestampRequest => sender ! this.timestamp
    case _: TIDRequest => sender ! this.tid
    case _ => { }
  }

  override def preStart() = {
    begin
    body
    commit
  }

} // Transaction class

