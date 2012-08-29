import scala.collection.mutable.HashMap

import java.util.concurrent.Semaphore

class LockTable {

  class Lock (val tid: Int, val shared: Boolean = false) {
    // TODO
    val sem = new Semaphore(0)
  } // Lock

  // perhaps change to a thread-safe map
  private val locks = new HashMap[Int, Lock]

  /** Lock */
  def x (tid: Int, oid: Int) {
    var lock: Lock = null
    var wait = false

    this.synchronized {
      if (locks contains oid) {
	wait = true
	lock = locks(oid)
      } // if
    } // synchronized

    if (wait) lock.sem.acquire()

    this.synchronized {
      locks(oid) = new Lock(tid)
    } // synchronized

  } // x

  /** Unlock */
  def u (tid: Int, oid: Int) {

    var lock: Lock = null
    var error = false

    this.synchronized {
      if (locks contains oid) {
	lock = locks(oid)
	if (lock.tid == tid) {
	  // TODO maybe the order of the following statements matteres
	  lock.sem.release()
	  locks -= oid
	} // if
      } else {
	error = true
      } // if
    } // synchronized

  } // u

} // LockTable


object LTTest extends App {

  val lt = new LockTable()

  lt.x(1,0)
  lt.x(1,1)
  lt.x(2,2)

//  lt.u(1,0)
  lt.u(1,1)

  lt.x(1,0)

} // LTTest
