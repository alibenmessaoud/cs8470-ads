package ads

import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer

import OpMessages._
import Op._

/**
 * A database transaction
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param tid The transaction identifier.
 * @param m The transaction manager object.
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
  private def begin () = m ! BeginMessage(this)

  /**
   * Read a value into the transaction from the database.
   *
   * @param oid The object identifier.
   * @return the value of the object.
   */
  private def read (oid: Int) = m !? ReadMessage(this, oid)
    
  /**
   * Write a value into the database.
   *
   * @param oid The object identifier.
   * @param value The value to write into the database.
   */
  private def write (oid: Int, value: Any) = m ! WriteMessage(this, oid, value)

  /**
   * Commit this transaction.
   */
  private def commit () = {
    m ! CommitMessage(this)
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


