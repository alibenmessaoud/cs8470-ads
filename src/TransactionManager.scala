import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer
import collection.mutable.ListBuffer
import Op._

/* Authors: Terrance Medina & Michael Cotterell
 *
 *		TransactionManager:
 *			Handles operation requests from the various Transaction threads.
 *			Reads operation requests from a buffer?
 *			Checks each incoming operation for CSR/2PL/TSO/MVCC schedulability.
 *
 *		Members:
 *			execute: performs the requested operation in the TMs data cache,
 *				NOT the persistent database
 *			rollback: undo the modifications to the Object HashMap
 *				what else?? store in a delay buffer and hold all other
 *				requests from this Transaction? We don't want cascading rollbacks.
 *
 */
class TransactionManager () extends Actor with ConcurrencyControl  
{

  /**
   *	Incoming operation buffer.
   */
  private val opBuffer = new ListBuffer[(Transaction, Op, Int)]()


  def act () = loop {
    receive {

      case bMsg: beginMessage => {
	
      }

      case rMsg: readMessage => {

      }

      case wMsg: writeMessage => {
	
      }

      case cMsg: commitMessage => {

      }
      
    } // recieve
  } // act


} // TransactionManager

