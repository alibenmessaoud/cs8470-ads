import scalation.math.Vectors._
import scalation.random.Random
import scalation.plot.Plot

class MovingAverage(y:VectorD, t:VectorD)
{
  def ma (k: Int): VectorD = {
    val z = new VectorD(y.dim - k)
    for (i <- 0 until z.dim) {
      z(i) = y(i to i + k).sum / (k + 1)
    } // for
    z
  }
}

object MATest extends App
{
  val n = 100
  val r = Random()
  val y = new VectorD(n)
  val t = new VectorD(n)
  for(i <- 0 until n)
  {
    t(i) = i.toDouble
    y(i) = t(i)+10*r.gen
  }
  val m = new MovingAverage(y, t)
  new Plot(t, y, m.ma(7))

}
