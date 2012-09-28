import collection.mutable.Set
class PrecedenceGraph(n: Int)
{
	val graph = new Array[Set[Int]](n)
	val color = new Array[Char](n)

	for(i<-0 until n)
	{
		graph(i) = Set[Int]()
		color(i) = 'G'
	}

	def addEdge(i:Int, j:Int)
	{
		graph(i)+=j
	}

	def hasCycle:Boolean = //equal sign must be specified if there is a return value
	{
		for(i <- 0 until n if color(i)=='G' && loopback(i)) return true
		return false
	}

	def loopback(i:Int):Boolean = 
	{
/*
			//check for Y
			if( color(i) == 'Y') return true
			//set color to yellow
			color(i) = 'Y'
			//for each child, if not red, check for loop
		//for each child of this node
		for(j<-graph(i))
		{
			if( color(j) != 'R' ) loopback(j)
		} 
			//set to Red
			color(i) = 'R'
			false
		//
		false
*/
 		if (color(i) == 'Y') return true
        color(i) = 'Y'
        for (j <- graph(i) if color(j) != 'R' && loopback (j)) return true
        color(i) = 'R'
        false
	}
}

object PGTest extends App
{
	val pg = new PrecedenceGraph(3)
	pg.addEdge(0,1)
	pg.addEdge(1,0)
	pg.addEdge(0,2)
	println("hasCycle = " + pg.hasCycle)
}
