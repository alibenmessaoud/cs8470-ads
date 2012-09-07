import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer

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
class Transaction (tid: Int, m: TransactionManager) extends Actor {

  private var randOps = false
  private var randNum = 0

  def this (tid: Int, m: TransactionManager, rand: Int) {
    this(tid, m, null)
    randOps = true
    randNum = rand
  } // this

  /**
   * The body of the transaction, if defined.
   */
  def body () { }  

  /**
   * Begin the transaction.
   */
  private def begin () = m ! (this, Op.Begin)

  /**
   * Read a value into the transaction from the database.
   *
   * @param oid The object identifier.
   * @return the value of the object.
   */
  private def read (oid: Int) = m !? (this, Op.Read, oid)
    
  /**
   * Write a value into the database.
   *
   * @param oid The object identifier.
   * @param value The value to write into the database.
   */
  private def write (oid: Int, value: Any) = m ! (this, Op.Write, oid, value)

  /**
   * Commit this transaction.
   */
  private def commit () = m ! (this, Op.Commit)

  /**
   * Rollback this transaction.
   */
  private def rollback () = m ! (this, Op.Rollback)

} // Transaction class

/**
 * Test object for Transaction
 */
object TxnTest extends App {

  val t1 = new Transaction(1, m) {
    override def body () {
      val x = r(100)
      x.bank = 12
      write(100, x)
    } // body
  }

} // TxnTest
