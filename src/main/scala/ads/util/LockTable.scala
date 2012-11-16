package ads
package util

/**
 * @author  John A. Miller
 * @version 1.0
 */

import scala.collection.mutable.HashMap
import java.util.concurrent.Semaphore

/**
 * This class implements a lock table as a hash map.  The object identifier (oid)
 * is used as the key to find the lock in the hash table.  If the lock is not found,
 * the data object is not locked.  This class can be used in the implementation
 * of locking protocols, such as Two-Phase Locking (2PL).
 * Caveat: shared/read locks are currently not implemented.
 */
class LockTable {

  /**
   * This class is used to represent an individual lock.
   * @param tid     the id of the transaction holding the lock
   * @param shared  whether the lock is shared (true) or exclusive (false)
   */
  class Lock (val tid: Int, val shared: Boolean = false) {
    val sem = new Semaphore (0)
  } // Lock class

  /** Associative map of locks held by transactions of the form (key = oid, value = lock)
   */
  private val locks = new HashMap [Int, Lock] ()

  /**
   * Acquire a shared/read lock on data object oid.
   * @param tid  the transaction id
   * @param oid  the data object id
   */
  def rl (tid: Int, oid: Int) {
    synchronized {
      // not yet implemented
    } // synchronized
  } // rl

  /**
   * Acquire an exclusive/write lock on data object oid.
   * @param tid  the transaction id
   * @param oid  the data object id
   */
  def wl (tid: Int, oid: Int) {

    var lock: Lock = null
    var wait = false

    synchronized {
      try {
        lock = locks(oid)             // find the lock
        wait = true
      } catch {
        case ex: Exception =>         // lock not found, so oid is not locked
      } // try
    } // synchronized

    if (wait) lock.sem.acquire ()         // wait for the lock to be released

    synchronized {
      locks(oid) = new Lock (tid)       // establish the lock in the table
    } // synchronized
  } // wl

  /**
   * Unlock/release the lock on data object oid.
   *  TODO: cycle through mailbox and activate all waiting readers
   * @param tid  the transaction id
   * @param oid  the data object id
   */
  def ul (tid: Int, oid: Int) {

    var lock: Lock = null
    var error = false

    synchronized {
      try {
        lock = locks(oid)                    // find the lock
      } catch {
        case ex: Exception => error = true   // lock not found
      } // try
      if (lock.tid == tid) {
        lock.sem.release ()                  // signal waiting transactions
        locks -= oid                         // remove the lock from table
      } else {
        error = true                         // lock not held/owned by tid
      } // if
    } // synchronized
    if (error) println ("Error: ul: no lock for oid = " + oid + " found/owned")
  } // ul

  /**
   * Convert the lock table to a string.
   */
  override def toString: String = {
    synchronized {
      "LockTable ( " + locks + " )"
    } // synchronized
  } // toString

} // LockTable class


