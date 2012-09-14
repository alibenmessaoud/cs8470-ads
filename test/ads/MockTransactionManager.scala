package ads.test

import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer

import ads.Transaction
import ads.TransactionManager
import ads.concurrency._
import ads.message._
import ads.util._
import ads.Op._

/**
 * Mockup Transaction Manager for Unit Testing
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
class MockTransactionManager() extends TransactionManager// with ConcurrencyControl with Traceable[TransactionManager] 
{

  TRACE = false
  
  // start when created
  this.start

  /**
   * 	Incoming operation buffer.
   */
  val opBuffer = new ListBuffer[(Transaction, Op, Int)]()
  val msgBuffer = new ListBuffer[(Message)]()

  override def act() = loop {
     
    react {
      case bMsg: BeginMessage => {
        trace("Message recieved: %s".format(bMsg))
        msgBuffer.append(bMsg)
      }

      case rMsg: ReadMessage => {
        trace("Message recieved: %s".format(rMsg))
        msgBuffer.append(rMsg)

      }

      case wMsg: WriteMessage => {
        trace("Message recieved: %s".format(wMsg))
        msgBuffer.append(wMsg)
      }

      case cMsg: CommitMessage => {
        trace("Message recieved: %s".format(cMsg))
        msgBuffer.append(cMsg)
      }

    } // react 
  } // act

} // TransactionManager

