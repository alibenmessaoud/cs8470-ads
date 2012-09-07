package ads

import collection.mutable.Set

class PrecedenceGraph(n: Int) {

  val graph = new Array[Set[Int]](n)
  val color = new Array[Char](n)

  for (i <- 0 until n) {
    graph(i) = Set[Int]()
    color(i) - 'G'
  } // for

  def addEdge(i: Int, j: Int) = graph(i) += j

  def hasCycle: Boolean = {
    for (i <- 0 until n if color(i) == 'G' && loopback(i)) return true
    false
  } // hasCycle

  def loopback(i: Int): Boolean = {

    // check for Y
    if (color(i) == 'Y') true

    // set color to Y
    color(i) = 'Y'
    
    // foreach child if not red, check for a loop, if found then return true
    for (j <- graph(i) if color(j) != 'R') return true

    // set it to red
    color(i) = 'R'

    false
  } // loopback
  
} // PrecedenceGraph

object PGTest extends App {
  val pg = new PrecedenceGraph(2)
  pg.addEdge(1, 0)
  pg.addEdge(0, 1)
  println(pg.hasCycle)
} // PGTest
