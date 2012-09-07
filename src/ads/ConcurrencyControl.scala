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

/**
 * Implementation of Serialization Graph Checking
 *
 * @author Michael E. Coterell
 * @author Terrance Medina
 */
trait SGC extends ConcurrencyControl 
{

  override def check(op : (Int, Op, Int)) : Boolean = {
    //do forever
    //read incoming operation from buffer

    //lookup object in Object HashMap

    //foreach operation in List
    
    //if it conflicts add edge to PrecedenceGraph

    //check PrecedenceGraph for cycles

    //if cycle, rollback(op_i)

    //else execute(op_i)
    true
  } // check

} // SGC

/**
 * Implementation of Two-Phase Locking
 *
 * @author Michael E. Coterell
 * @author Terrance Medina
 */
trait TwoPL extends ConcurrencyControl { 

} // TwoPL

/**
 * Implementation of Time Stamp Ordering
 *
 * @author Michael E. Coterell
 * @author Terrance Medina
 */
trait TSO extends ConcurrencyControl {

} // TSO

/**
 * Implementation of Multiversion Version Concurrency Control
 *
 * @author Michael E. Coterell
 * @author Terrance Medina
 */
trait MVCC extends ConcurrencyControl {

} // MVCC

