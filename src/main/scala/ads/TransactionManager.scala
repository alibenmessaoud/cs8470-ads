package ads

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ListBuffer

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.actor.Props
import akka.event.Logging

import ads.concurrency.ConcurrencyControl
import ads.message._
import ads.util._
import ads.Op._

/**
 * Handles operation requests from the various Transaction threads.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
class TransactionManager() extends Actor with ConcurrencyControl {

  /**
   * Used for trace statements
   */
  private val trace = Logging(context.system, this) 

  /**
   * 	Incoming operation buffer.
   */
  private val opBuffer = new ListBuffer[(Transaction, Op, Int)]()

  def receive = {

    case bMsg: BeginMessage => {
      trace.info("Message recieved: %s".format(bMsg))
    }

    case rMsg: ReadMessage => {
      trace.info("Message recieved: %s".format(rMsg))

      if (check(rMsg.t, Op.Read, rMsg.oid)) {
        trace.info("Request Granted: %s".format(rMsg))
      } else {
        trace.warning("Request Denied: %s".format(rMsg))
      } // if

      sender ! "hello"
    } // case

    case wMsg: WriteMessage => {
      trace.info("Message recieved: %s".format(wMsg))
      if (check(wMsg.t, Op.Write, wMsg.oid)) {

	trace.info("Request Granted: %s".format(wMsg))	

	// send back an okay response
        sender ! WriteResponse(false)

      } else {

	sender ! WriteResponse(true)
	trace.warning("Request Denied: %s".format(wMsg))

      }// if
    } // case

    case cMsg: CommitMessage => {
      trace.info("Message recieved: %s".format(cMsg))
    }

  } // recieve

} // TransactionManager

