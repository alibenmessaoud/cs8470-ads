import collection.mutable.ListBuffer
import Op._

/* Authors: Terrance Medina & Michael Cotterell
 *
 *		TransactionManager:
 *			Handles operation requests from the various Transaction threads.
 *			Reads operation requests from a buffer?
 *			Checks each incoming operation for CSR/2PL/TSO/MVCC schedulability.
 *
 *		Members:
 *			execute: performs the requested operation in the TMs data cache,
 *				NOT the persistent database
 *			rollback: undo the modifications to the Object HashMap
 *				what else?? store in a delay buffer and hold all other
 *				requests from this Transaction? We don't want cascading rollbacks.
 *
 */
class TransactionManager () extends ConcurrencyControl  
{

  /**
   *	Incoming operation buffer.
   */
  private val opBuffer = new ListBuffer[(Transaction, Op, Int)]()
  def begin (tid: Int) { }

  def read (tid: Int, oid: Int): Array[Any] = null

  def write (tid: Int, oid: Int, value: Array[Any]) { }

  private def rollback (tid: Int) { }

	//Not needed until Project2
  def commit (tid: Int) { }

	def request(op : (Transaction, Op, Int)):Boolean



} // TransactionManager

object TxnTest extends App 
{

  val tm = new TransactionManager ()

  val t1 = new Transaction (1, tm, 10)


}
