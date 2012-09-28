import scalation.math.Vectors._
import scalation.random.Random
import scalation.plot.Plot

class MovingAverage(y:VectorD, t:VectorD)
{
  def ma(k:Int) : VectorD = 
  {
     val z = new VectorD(y.dim-k) 

     for(i <- 0 until z.dim)
     {
       z(i) = (y(i until i+k).sum/(k+1))
     }
     //return
     z

  }

  
}

object MATest extends App
{
  import StatVecDConvert._
  val n = 200
  val r = Random()
  val y = new VectorD(n)
  val t = new VectorD(n)
  val z = new VectorD(n-2)

  for(i <- 0 until n)
  {
    t(i) = i.toDouble
    y(i) = t(i)+10*r.gen
  }
  val mu = y.mean
  val yy = y - mu

  for(i <- 0 until n-3)
  {
    //z(i) = yy.autocor(1)*yy(i) + yy.autocor(2)*yy(i+1)
    z(i) = yy.autocor(1)*yy(i+2) + yy.autocor(2)*yy(i+1) + yy.autocor(3)*yy(i)
    println("autocor on y(1) "+yy.autocor(i))
  }
  val m = new MovingAverage(y, t)

  new Plot(t, y, z+mu)

}

object StatVecDConvert
{
  implicit def mkStatVecD(x:VectorD) :StatVecD =
  {
    new StatVecD(x)
  }
}
class StatVecD(x:VectorD)
{
  import StatVecDConvert._
  def mean = x.sum/x.dim.toDouble 
  def variance = (((x-mean)*(x-mean)).sum)/(x.dim -1).toDouble
  def stdev2 = variance
  def stdev = math.sqrt(variance)
  def covariance(y: VectorD) = (((x-mean)*(y-y.mean)).sum)/(x.dim-1).toDouble
  def cor(y : VectorD) = covariance(y)/(x.stdev*y.stdev)
  def autocor(k : Int) = 
  {
    val a = x(k until x.dim)
    val b = x(0 until x.dim-k)
    a.cor(b)
  }
  
}
object Foobar extends App
{
  import StatVecDConvert._
  val x : VectorD = new VectorD(9., 15., 25., 14.,10.,18.,0.,16.,5.,19.,16.,20.)
  val y : VectorD = new VectorD(39.,56.,93.,61.,50.,75.,32.,85.,42.,70.,66.,80.)

  println("x.mean "+x.mean)
  println("y.mean "+y.mean)
  println("x.var "+x.variance)
  println("y.var "+y.variance)
  println("x.stddev2 "+x.stdev2)
  println("y.stddev2 "+y.stdev2)
  println("x.covar wrt y"+x.covariance(y))
  println("y.covar wrt x"+y.covariance(x))
  println("x.cor wrt y "+x.cor(y))
  println("y.cor wrt x" +y.cor(x))
  println("x.autocor of 1 "+x.autocor(1))
  println("y.autocor of 1 "+y.autocor(1))
  println("x.autocor of 2 "+x.autocor(2))
  println("y.autocor of 2 "+y.autocor(2))
  println("x.autocor of 3 "+x.autocor(3))
  println("y.autocor of 3 "+y.autocor(3))
}
