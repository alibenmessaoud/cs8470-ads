import scala.collection.mutable.{HashMap, SynchronizedMap}

import java.util.concurrent.Semaphore

class LockTable {

  class Lock (val tid: Int, val shared: Boolean = false) {
    // TODO
    val sem = new Semaphore(0)
  } // Lock

  // perhaps change to a thread-safe map
  private val locks = new HashMap[Int, Lock] with SynchronizedMap[Int, Lock]

  /** Lock */
  def x (tid: Int, oid: Int) {

    var wait = false
    val lock = locks get oid

    lock match {
      case None       => {}
      case Some(lock) => {
	wait = true
	lock.sem.acquire()
      }
    } // match

    locks(oid) = new Lock(tid)

  } // x

  /** Unlock */
  def u (tid: Int, oid: Int) {

    var error = false

    val lock = locks get oid

    lock match {
      case None       => error = true
      case Some(lock) => if (lock.tid == tid) {
	locks -= oid
	lock.sem.release()
      }
    } // match

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
