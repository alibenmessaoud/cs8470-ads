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
 * @param body The set of statements to be executed by this transaction.
 */
class Transaction (var tid: Int, m: TransactionManager) extends Actor {

  //private var randOps = false
  protected var randNum = 0

/*
  def this (tid: Int, m: TransactionManager, rand: Int) {
    this(tid, m, null)
    randOps = true
    randNum = rand
		
  } // this
*/

	def body() {}
  def act () { } // act



  /**
   * Begin the transaction.
   */
  def begin () = m ! BeginMessage(this)

  /**
   * Read a value into the transaction from the database.
   *
   * @param oid The object identifier.
   * @return the value of the object.
   */
  def read (oid: Int) = m !? ReadMessage(this, oid)
    
  /**
   * Write a value into the database.
   *
   * @param oid The object identifier.
   * @param value The value to write into the database.
   */
  def write (oid: Int, value: Any) = m ! WriteMessage(this, oid, value)

  /**
   * Commit this transaction.
   */
  def commit () = {
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

class ExplicitTransaction (var _tid: Int, _m: TransactionManager, explicit_body: ()=>Unit) extends Transaction(_tid, _m) {
	override def act()
	{
		explicit_body()
	}
}
class RandomTransaction (var _tid: Int, _m: TransactionManager, rand: Int) extends Transaction(_tid, _m) {
	
	override def act()
	{
		var i = 0
		while(i<rand){println("Hello!");i=i+1;}
		
	}
}
