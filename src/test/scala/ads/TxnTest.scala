import java.util.Random

import ads._
import ads.concurrency._

object TxnTest extends App {

  val rand = new Random()

  val tm = new TransactionManager with SGC
  
  val txns = for (i <- 1 to 10) yield new Transaction(tm) {
    override def body() {
      val x = read(7)
      write(7, 5)
    }
  }

  txns.par foreach (t => {
    t.start
  })

}