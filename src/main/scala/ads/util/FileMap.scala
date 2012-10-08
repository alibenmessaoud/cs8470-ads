package ads.util

import scala.collection.mutable.{ HashMap, Map }

import java.io.RandomAccessFile

import akka.event.{ Logging, LogSource }
import akka.actor.ActorSystem

import ads.{ Database, Page }
import ads.schema._

/**
 * Companion object for FileMap class
 */
object FileMap {

  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = "FileMap(\"%s\")".format(o.asInstanceOf[FileMap].name)
    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

} // FileMap

class FileMap (val name: String, val filename: String, val schema: Schema) extends Map [Int, Array[Property[_]]] {

  // akka logger
  private val trace = Logging(schema.db.system, this)

  /**
   * The page size
   */
  private val PAGE_SIZE = 10

  private val datFile = new RandomAccessFile(filename, "rw")
  private val cache   = new HashMap[Int, Array[Property[_]]]()
  private val pages   = new HashMap[Int, Page]

  trace.info("%s initialized".format(this))

  /**
   * Write modified, non-dirty pages to the file
   */
  def flush: Unit = {

    trace.info("flushing")

    // TODO need to prevent flusing uncommitted pages
    for (page <- pages.values if page.modified) {

      trace.info("writing page %d to disk".format(page.pid))
      
      // seek to the appropriate place
      datFile.seek(page.pid * schema.width * PAGE_SIZE)

      for (i <- 0 until PAGE_SIZE) {

	val oid = (page.pid * PAGE_SIZE) + i

	trace.info("writing oid = %d to disk")

	for (prop <- cache(oid)) {
	  datFile.write(prop.getAsByteArray)
	} // for

	trace.info("finished writing oid = %d to disk")

      } // for

      trace.info("finished writing page %d to disk".format(page.pid))

    } // for

  } // flush
  
  /**
   * Reorganize the files so that they are in order
   */
  def optimize: Unit = {

    trace.info("optimizing")

  } // optimize

  /**
   * Returns the page where a record should live
   */
  private def getPage (oid: Int): Page = {

    trace.info("Page for oid = %d requested".format(oid))
    
    // calculate which page the record should be in
    val pid = (oid * schema.width) / (schema.width * PAGE_SIZE)

    pages.get(pid) match {
      case Some(page) => {} // do nothing
      case None       => readPage(pid)
    } // match

    // return the page
    pages(pid)

  } // getPage

  /**
   * Reads a page into the cache from disk
   */
  private def readPage (pid: Int): Unit = {

    trace.info("Reading page %d from disk".format(pid))
    
    // seek to the appropriate place
    datFile.seek(pid * schema.width * PAGE_SIZE)
    trace.info("Seeking to %d".format(pid * schema.width * PAGE_SIZE))

    // read in the values
    for (i <- 0 until PAGE_SIZE) {

      val array = Array.ofDim[Property[_]](schema.size)
      var j     = 0

      // calculate oid for the object about to be read
      val oid = (pid * PAGE_SIZE) + i

      trace.info("reading %d bytes from disk for record with oid = %d".format(schema.width, oid))

      // read them in property by property
      for (p <- schema.properties.values.toArray) {

	trace.info("reading bytes for %s".format(p))

	val bytes = Array.ofDim[Byte](p.width)
	val prop: Property[_] = p.makeClone

	datFile.read(bytes)

	trace.info("read bytes: %s".format(bytes.deep))
	
	prop.setFromByteArray(bytes)

	trace.info("value = %s".format(prop.get))

	// add the cloned property to the array
	array(j) = prop
	j += 1

      } // for

      trace.info("finished reading record with oid = %d".format(oid))

      // add the array to the cache
      cache(oid) = array

    } // for

    trace.info("finished reading page %d".format(pid))

    // update the page
    pages(pid) = new Page(pid)

  } // readPage

  def get (key: Int): Option[Array[Property[_]]] = {

    val p = getPage(key)

    // create the array
    val array = cache(key)

    // return the array
    Some(array)

  } // get

  def iterator: Iterator[(Int, Array[Property[_]])] = null
  def + (kv: (Int, Array[Property[_]])) = null

  def += (kv: (Int, Array[Property[_]])) = {

    val (oid, props) = kv
    val p = getPage(oid)

    p.modified = true
    p.dirty = true

    cache(oid) = props

    this

  } // 

  override def - (key: Int) = null
  def -= (key: Int) = null

  override def empty = null

  override def toString = "FileMap(name = \"%s\", filename = \"%s\", schema = %s)".format(name, filename, schema)

} // FileMap

object FileMapTest extends App {

  val db = new Database("TestDB")

  class PersonSchema () extends Schema ("Person", db) {
    register(IntProperty("id"))
    register(StringProperty("name", 32))
  } // PersonSchema

  val schema = new PersonSchema()

  schema.printSchema

  val fm = new FileMap("person", "person.dat", schema)

  val array = fm(20)

  println(array.deep)

  println(array(0)) //.asInstanceOf[IntProperty].set(20)
  println(array(1)) //.asInstanceOf[StringProperty].set("michael")

  array(0).asInstanceOf[StringProperty].set("michael")
  array(1).asInstanceOf[IntProperty].set(20)

  fm += 20 -> array

  fm.flush
  
} // FileMapTest
