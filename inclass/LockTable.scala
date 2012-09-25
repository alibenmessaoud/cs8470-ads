import scala.collection.mutable.HashMap
import java.util.concurrent.Semaphore

class LockTable
{
	/*
	 * Lock: defines the locks to be stored in the LockTable as semaphores.
	 */
	class Lock(val tid: Int, val shared: Boolean = false)
	{
		val sem = new Semaphore(0)
	}//Lock

	//The LockTable
	private val locks = new HashMap[Int, Lock]

	/*
	 * Exclusive Lock
	 */
	def x(tid: Int, oid: Int)
	{
		var lock: Lock = null
		var wait = false
		
		synchronized
		{
			wait = locks contains oid
			if(wait) lock=locks(oid)
		}//sync
			if(wait) lock.sem.acquire()
			else
			{
				synchronized
				{
					//add a new lock to the LockTable
					locks(oid) = new Lock(oid)
				}
			}
	}//exclusive lock
	
	/*
	 * Release Lock
	 */
	def u(tid: Int, oid: Int)
	{
		var lock: Lock = null
		var error = false
		
		if(locks contains oid)
		{
			lock = locks(oid)
			if(lock.tid == tid)
			{
				lock.sem.release()
				locks -= oid
			}
			else
			{
				error = true
			}
		}
	}//Release
}

	/*****************
		MAIN
	*****************/
	object LTTest extends App
	{
		val lt = new LockTable()
		lt.x(0,0)//T_0 locks object_0
		lt.x(1,1)
		lt.x(2,2)

		lt.u(0,0)
		lt.u(1,0)
		lt.u(2,0)
	}//Main
