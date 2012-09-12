package ads
package concurrency

import collection.mutable.{ListBuffer, ListMap}

import ads.util.PrecedenceGraph
import ads.Op._

/**
 * Implementation of Serialization Graph Checking
 *
 * @author Michael E. Coterell
 * @author Terrance Medina
 */
trait SGC extends ConcurrencyControl 
{

  /**
   * Recent operations that have been checked.
   */
  val recent = ListMap.empty[Int, ListBuffer[(Int, Op)]]

  /**
   * The precedence graph used for checking.
   */
  val pg = new PrecedenceGraph()

  override def check(t: Transaction, opType: Op, oid: Int): Boolean = {

    val tid = t.tid

    recent.get(oid) match {

      case None       => recent += oid -> ListBuffer((tid, opType))

      case Some(list) => {

	// check for conflicts, adding to the precedence graph as needed
	for (item <- list) {
	  val (tid2, opType2) = item
	  if (tid != tid2 && (opType == Op.Write || opType2 == Op.Write)) pg.addEdge(tid2, tid)
	} // for

	// if the graph has a cycle then we need to do some house cleaning and
	// return false, otherwise we add the operation to the recent list and
	// return true
	if (pg.hasCycle) {

	  pg.removeVertex(tid)
	  t.rollback
	  return false

	} // if

	list += ((tid, opType)) 

      } // case

    } // match

    true
  } // check

} // SGC
