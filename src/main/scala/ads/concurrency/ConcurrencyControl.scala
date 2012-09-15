package ads.concurrency

import akka.actor.ActorRef
import akka.util.Timeout
import akka.util.duration._

import ads.Op._

/** 
 * Handles operation request from TransactionManager. Behavior defined by
 * trait mixins.
 * 
 * @author Michael E. Coterell
 * @author Terrance Medina
 */
trait ConcurrencyControl {

  /**
   * The timeout for actor responses
   */
  implicit val timeout = Timeout(5 seconds)

  /**
   * Checks if the given operation is schedulable.		
   *	
   * @param op A triple of {TransactionID, Operation Type, Object ID}
   * @return True if operation is schedulable, False otherwise	
   */
  def check(t: ActorRef, opType: Op, oid: Int): Boolean = true

} // ConcurrencyControl
