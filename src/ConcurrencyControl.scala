/** 
 * Handles operation request from TransactionManager. 
 *	Behavior defined by trait mixins.
 * 
 * @author Michael Coterell
 * @author Terrance Medina
 *	
 */
trait ConcurrencyControl {
//Check Schedulability
	/**
	 * Checks if the given operation is schedulable.		
    *	
    * @param op A triple of {TransactionID, Operation Type, Object ID}
	 * @return True if operation is schedulable, False otherwise	
	 */
	def check(op : (Int, Op, Int)) : Boolean = True

} // ConcurrencyControl

trait CSR extends ConcurrencyControl
{
	override def check(op : (Int, Op, Int)) : Boolean 
	{
	//do forever
		//read incoming operation from buffer

		//lookup object in Object HashMap

		//foreach operation in List
	
			//if it conflicts add edge to PrecedenceGraph

				//check PrecedenceGraph for cycles

					//if cycle, rollback(op_i)

					//else execute(op_i)
		True
	}
}//end CSR

trait TwoPL extends ConcurrencyControl
{
}
trait TSO extends ConcurrencyControl
{
}
trait MVCC extends ConcurrencyControl
{
}

