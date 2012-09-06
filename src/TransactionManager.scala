import collection.mutable.ListBuffer
import Op._

class TransactionManager () extends ConcurrencyControl  
{

  private val recent = new ListBuffer[(Transaction, Op, Int)]()
  def begin (tid: Int) { }


  def read (tid: Int, oid: Int): Array[Any] = null

  def write (tid: Int, oid: Int, value: Array[Any]) { }

  def rollback (tid: Int) { }

  def commit (tid: Int) { }

} // TransactionManager

object TxnTest extends App 
{

  val tm = new TransactionManager ()

  val t1 = new Transaction (1, tm, 10)


}
