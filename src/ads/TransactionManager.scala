package ads

import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer
import collection.mutable.ListBuffer

import concurrency._
import message._
import Op._

/**
 * Companion object for TransactionManager
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
object TransactionManager {
  val TRACE = true
} // TransactionManager

/**
 * Handles operation requests from the various Transaction threads.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
class TransactionManager() extends Actor with ConcurrencyControl {

  // start when created
  this.start

  /**
   * 	Incoming operation buffer.
   */
  private val opBuffer = new ListBuffer[(Transaction, Op, Int)]()

  private def trace(msg: String) = if (TransactionManager.TRACE) {
    println("[TransactionManager] %d - %s".format(System.currentTimeMillis, msg))
  } // trace

  def act() = loop {
    react {

      case bMsg: BeginMessage => {
        trace("Message recieved: %s".format(bMsg))
      }

      case rMsg: ReadMessage => {
        trace("Message recieved: %s".format(rMsg))
        rMsg.t ! ""
        if (check(rMsg.t, Op.Read, rMsg.oid)) {
          // TODO
        } // if

      }

      case wMsg: WriteMessage => {
        trace("Message recieved: %s".format(wMsg))
        if (check(wMsg.t, Op.Write, wMsg.oid)) {
          // TODO
        } // if
      }

      case cMsg: CommitMessage => {
        trace("Message recieved: %s".format(cMsg))
      }

    } // recieve
  } // act

} // TransactionManager

