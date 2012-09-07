package ads
package concurrency

import Op._

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
