package ads.test

import org.scalatest.FunSuite
import ads.TransactionManager
import ads.util.PrecedenceGraph
/*
 * Tests the Precedence Graph utility.
 */
class PGTest extends FunSuite
{
  test("hasCycle = true")
  {
     val pg = new PrecedenceGraph
     pg.addEdge(0,1)
     pg.addEdge(1,2)
     pg.addEdge(1,3)
     pg.addEdge(2,1)
     assert (pg.hasCycle == true)
  }
  test("addEdge Creates Cycle")
  {
     val pg = new PrecedenceGraph
     pg.addEdge(0,1)
     pg.addEdge(1,2)
     pg.addEdge(1,3)
     pg.addEdge(2,3)
     assert (pg.hasCycle == false)
     pg.addEdge(3,1)
     assert (pg.hasCycle == true)
  }
  test("Remove Vertex kills cycle")
  {
     val pg = new PrecedenceGraph
     pg.addEdge(0,1)
     pg.addEdge(1,2)
     pg.addEdge(1,3)
     pg.addEdge(2,1)
     assert (pg.hasCycle == true)
     pg.removeVertex(2)
     assert (pg.hasCycle == false)
  }
  test("hasCycle = false")
  {
     val pg = new PrecedenceGraph
     pg.addEdge(0,1)
     pg.addEdge(1,2)
     pg.addEdge(1,3)
     pg.addEdge(2,3)
     assert (pg.hasCycle == false)
  }




}
