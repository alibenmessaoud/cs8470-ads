package ads.concurrency

import scala.collection.mutable.{ListBuffer, ListMap}

import akka.actor.ActorRef
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._

import ads.{Op, Transaction}
import ads.util.PrecedenceGraph
import ads.message.CheckResponse
import ads.message.CheckResponses._
import ads.Op._

/**
 * Companion object for SGC trait
 */
object SGC {

  val CLEANUP_COUNT = 10

} // SGC

/**
 * Implementation of Serialization Graph Checking
 *
 * @author Michael E. Coterell
 * @author Terrance Medina
 */
trait SGC extends ConcurrencyControl {

  private var cleanup = 0

  /**
   * Recent operations that have been checked.
   */
  val recent = ListMap.empty[Int, ListBuffer[(Int, Op)]]

  /**
   * The precedence graph used for checking.
   */
  val pg = new PrecedenceGraph()

  override def check(t: Transaction, opType: Op, oid: Int): CheckResponse = {

    println("checking %s request for %s on %d".format(t, opType, oid))

    val tid = t.getTID.get

    recent.get(oid) match {

      case None => recent += oid -> ListBuffer((tid, opType))

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
          return Rollbacked

        } // if

	// If the operation is a commit and it's made it this far then we don't
	// even need to add it to the list. Instead, we'll do some cleanup.
	if (opType == Op.Commit) {
	  cleanup(tid)
	} else {
          list += ((tid, opType))
	} // if

      } // case

    } // match

    Granted
  } // check

  /**
   * Perform rudamentary garbage collection
   *
   * @param tid The transaction identifier to cleanup
   */
  private def cleanup (tid: Int) {

    // remove instances of tid from the recent transaction list
    for (i <- recent.values) {
      i --= i.filter( e => e._1 == tid )
    } // for

    // remove tid from graph
    pg.removeVertex(tid)

    // reset the counter
    cleanup = 0

  } // cleanup

} // SGC
