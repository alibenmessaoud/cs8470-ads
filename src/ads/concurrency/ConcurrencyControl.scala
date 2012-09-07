package ads

import Op._

/** 
 * Handles operation request from TransactionManager. Behavior defined by
 * trait mixins.
 * 
 * @author Michael E. Coterell
 * @author Terrance Medina
 */
trait ConcurrencyControl {

  /**
   * Checks if the given operation is schedulable.		
   *	
   * @param op A triple of {TransactionID, Operation Type, Object ID}
   * @return True if operation is schedulable, False otherwise	
   */
  def check(op : (Int, Op, Int)) : Boolean = true

} // ConcurrencyControl

