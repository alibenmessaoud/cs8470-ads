class Record
{
	//TODO
	
}

class Page(m: Int)
{
	var accessed = 0 //last time of access
	var modified = false //last change of page
			//tells us if we should write out to filesystem
	var dirty = false //contains possibly unreliable data
			//data becomes clean after a commit
			//dirty data has been written, but not yet committed

	val contents = Array.ofDim[Record]	
}
