import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer
import Op._

/* Authors: Terrance Medina & Michael Cotterell
 *
 *		Transaction:
 *			Represents the thread objects that submit transaction requests to the TransactionManager.
 *			Members:
 *				begin(): Sends a 'begin' operation request to the TransactionManager
 *				read(): Sends a 'read' operation request to the TransactionManager
 *				write(): Sends a 'write' operation request to the TransactionManager
 *				commit(): Sends a 'commit' operation request to the TransactionManager
 *				
 *
 */
class Transaction (var tid: Int, m: TransactionManager) extends Actor {

  private var randOps = false
  private var randNum = 0

  def this (tid: Int, m: TransactionManager, rand: Int) {
    this(tid, m)
    randOps = true
    randNum = rand
  } // this

  def act () {
      
    if (randOps) {
      // TODO implement
    } else body()

  } // act

  /**
   * The body of the transaction, if defined.
   */
  def body () { }  

  /**
   * Begin the transaction.
   */
  private def begin () = m ! beginMessage(this)

  /**
   * Read a value into the transaction from the database.
   *
   * @param oid The object identifier.
   * @return the value of the object.
   */
  private def read (oid: Int) = m !? readMessage(this, oid)
    
  /**
   * Write a value into the database.
   *
   * @param oid The object identifier.
   * @param value The value to write into the database.
   */
  private def write (oid: Int, value: Any) = m ! writeMessage(this, oid, value)

  /**
   * Commit this transaction.
   */
  private def commit () = {
    m ! commitMessage(this)
    exit()
  } // commit

  /**
   * Rollback this transaction.
   */
  def rollback (tid: Int) = {

    this.tid = tid

    // TODO make it wait some random amount of time

    body()

  } // rollback

} // Transaction class

/**
 * Test object for Transaction
 */
object TxnTest extends App {


} // TxnTest
