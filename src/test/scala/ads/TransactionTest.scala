package ads.test

import org.scalatest.FunSuite
//import MockTransactionManager
import ads.Transaction
import ads.TransactionManager
import ads.message._
import scala.StringBuilder

/* TODO:
 *		These tests don't really test anything. I'll put in some asserts in a bit.
 */
class TransactionTest extends FunSuite
{
	val tm = new MockTransactionManager()

	test("TransactionManager receives messages from Transaction")
	{
		val t = new Transaction(tm)
		{
			override def body
			{
	    val x = read(7)
	    write(7, 5)
			}

      TRACE = false
		}
    t.start
    Thread.sleep(1000)
    assert(tm.msgBuffer.size == 4)
    assert(tm.msgBuffer.head == (BeginMessage(t)))
    tm.msgBuffer.remove(0)
    assert(tm.msgBuffer.size == 3)
    assert(tm.msgBuffer.head ==(ReadMessage(t, 7)))
    tm.msgBuffer.remove(0)
    assert(tm.msgBuffer.size == 2)
    assert(tm.msgBuffer.head ==(WriteMessage(t, 7, 5)))
    tm.msgBuffer.remove(0)
    assert(tm.msgBuffer.size == 1)
    assert(tm.msgBuffer.head ==(CommitMessage(t)))
    tm.msgBuffer.remove(0)
    assert(tm.msgBuffer.isEmpty)
	}

}
