import scalation.math.Vectors._
object Entropy
{
  def getEntropy(p : VectorD) : Double = 
  {
    var s = 0.
    for(i <- 0 until p.dim if p(i) > 0)
    {
      s += p(i)*math.log(p(i))/math.log(2)
    }
    -s
  }
  def getAvgEntropy(e : VectorD, w : VectorD) : Double =
  {
    (e * w).sum
  }


}


object EntropyTest extends App
{
  val result = new VectorD(0, 0, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0)
  val temp = new VectorD(2, 2, 2, 1, 0, 0, 0, 1, 0, 1, 1, 1, 2, 1)
  val outlook = new VectorD(2, 2, 1, 0, 0, 0, 1, 2, 2, 0, 2, 1, 1, 0)

  val p0 = new VectorD(3./5, 2./5)
  val p1 = new VectorD(2./5, 3./5)
  val p2 = new VectorD(1, 0)

  val e_outlook = new VectorD(Entropy.getEntropy(p0), Entropy.getEntropy(p1), Entropy.getEntropy(p2))
  val weights = new VectorD(5./14, 5./14, 4./14)

  println(Entropy.getAvgEntropy(e_outlook, weights))
  println(Entropy.getEntropy(new VectorD(1./3, 2./3)))
  //println(Entropy.getAvgEntropy(new VectorD(.811, .918, 1), new VectorD(4./14, 6./14, 4./14)))

}

