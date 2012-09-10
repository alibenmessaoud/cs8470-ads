package ads

import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer
import collection.mutable.ListBuffer

import concurrency._
import message._
import Op._

/**
 * Handles operation requests from the various Transaction threads.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 */
class TransactionManager () extends Actor with ConcurrencyControl  
{

  /**
   *	Incoming operation buffer.
   */
  private val opBuffer = new ListBuffer[(Transaction, Op, Int)]()


  def act () = loop {
    receive {

      case bMsg: BeginMessage => {
	
      }

      case rMsg: ReadMessage => {

      }

      case wMsg: WriteMessage => {

      }

      case cMsg: CommitMessage => {

      }
      
    } // recieve
  } // act


} // TransactionManager

