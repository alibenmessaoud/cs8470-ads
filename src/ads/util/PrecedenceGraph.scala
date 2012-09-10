package ads
package util

import collection.mutable.{ListMap, Set}

/**
 * A Precedence Graph.
 */
class PrecedenceGraph () {

  private val graph = ListMap.empty[Int, Set[Int]]
  private var color = Array.empty[Char]
  private var max   = 0

  def addEdge(i: Int, j: Int) = {

    if (j > max) max = j
    if (i > max) max = i

    graph.get(i) match {
      case None    => graph += i -> Set(j)
      case Some(s) => s.add(j)
    } // match

  } // adEdge

  def removeVertex(i: Int) = {
    graph -= i
    for (set <- graph.values) set.remove(i)
  } // removeVertex

  def hasCycle: Boolean = {
    color = Array.fill(max + 1)('G')
    for (i <- 0 to max if color(i) == 'G' && loopback(i)) return true
    false
  } // hasCycle

  def loopback(i: Int): Boolean = {

    // check for Y
    if (color(i) == 'Y') true

    // set color to Y
    color(i) = 'Y'

    // foreach child if not red, check for a loop, if found then return true
    graph.get(i) match {
      case None      => { }
      case Some(set) => for (j <- set if color(j) == 'R') return true
    }

    // set it to red
    color(i) = 'R'

    false
  } // loopback
  
} // PrecedenceGraph


