package ads
package util

import collection.mutable.{ListMap, Set}

/**
 * A Precedence Graph.
 *
 * @author Michael E. Cotterell
 */
class PrecedenceGraph () {

  private val graph = ListMap.empty[Int, Set[Int]]
  private var color = Array.empty[Char]
  private var max   = 0

  /**
   * Adds a directed edge to this precedence graph.
   *
   * @param i The first vertex in the directed edge
   * @param j The second vertex in the directed edge
   */
  def addEdge(i: Int, j: Int) = {

    if (j > max) max = j
    if (i > max) max = i

    graph.get(i) match {
      case None    => graph += i -> Set(j)
      case Some(s) => s.add(j)
    } // match

  } // adEdge

  /**
   * Removes a vertex from this precedence graph.
   *
   * @param i The vertex to be removed.
   */
  def removeVertex(i: Int) = {
    graph -= i
    for (set <- graph.values) set.remove(i)
  } // removeVertex

  /**
   * Returns true if this precedence graph has a cycle.
   *
   * @return true if a cycle exists, false otherwise.
   */
  def hasCycle: Boolean = {
    color = Array.fill(max + 1)('G')
    for (i <- 0 to max if color(i) == 'G' && loopback(i)) return true
    false
  } // hasCycle

  /**
   * Determines if there has been a loopback in the cycle checking yet.
   *
   * @param i The vertex to begin checking with.
   * @return true if there has been a loopback, false otherwise.
   */
  private def loopback(i: Int): Boolean = {

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


