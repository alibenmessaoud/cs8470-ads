package ads.schema

import scala.collection.mutable.{ HashMap, Map }

import akka.event.{ Logging, LogSource }
import akka.actor.ActorSystem

import ads.Database
import ads.util.FileMap

/**
 * Companion object for Schema class
 */
object Schema {

  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = "Schema(\"%s\")".format(o.asInstanceOf[Schema].getName)
    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

} // Schema

/**
 * A Database Table Schema
 */
class Schema (val _name: String, val db: Database) {

  // the FileMap for this schema
  lazy val fileMap = new FileMap(_name, "%s-%s.dat".format(db.name, _name), this)

  // akka logger
  val trace = Logging(db.system, this)

  // properties list
  val properties: Map[String, Property[_]] = new HashMap[String, Property[_]]()

  // register a property with this schema
  def register (p: Property[_]): Unit = {

    if (properties.contains(p.getName)) {
      trace.warning("Tried adding a duplicate property to this schema. Property name = \"%s\"".format(this, p.getName))
      return
    } // if

    // add the property to the list
    properties += p.getName -> p

    trace.info("registered %s".format(p))

  } // register

  /**
   * The byte width of this table schema
   */
  def width = properties.values.map(_.width).sum

  /**
   * The number of properties in this table schema
   */
  def size = properties.values.size

  override def toString = "Schema(name = \"%s\", width = %d)".format(_name, width)

  def getName = _name

  def printSchema: Unit = {
    println(this)
    println(" - name: %s".format(getName))
    println(" - db: %s".format(db.name))
    println(" - row byte width: %d".format(width))
    println(" - properties:")
    for (p <- properties.keys) println("    - %s -> %s".format(p, properties(p)))
  } // printSchema

  /**
   * Returns a copy of a property array with default values
   */
  def getPropArray: Array[Property[_]] = properties.values.map(p => {
    val np: Property[_] = p.makeClone
    np.setFromAny(p.getDefault)
    np
  }).toArray

  /**
   * Returns the row for a particular object
   *
   * @param oid the object identifier
   * @return the schema row for that object
   */
  def getById (oid: Int) = new SchemaRow(this, oid, fileMap)

  /**
   * Returns the row for a particular object
   *
   * @param oid the object identifier
   * @return the schema row for that object
   */
  def apply (oid: Int) = getById(oid)

  /**
   * Flush modified, non dirty objects in this table to disk
   */
  def flush = fileMap.flush

  /**
   * Returns the page id for an object
   */
  def getPageIdFor (oid: Int) = (oid * width) / (width * fileMap.PAGE_SIZE)

} // Schema

/**
 * This class represents a row in the table schema. It uses the Dynamic trait
 * in order to let us get elements of the row by name. You still need to cast
 * the result.
 *
 * @param schema the schema
 * @param oid the objet identifier for this row
 * @param fileMap the fileMap for the schema
 */
class SchemaRow (schema: Schema, oid: Int, fileMap: FileMap) extends Dynamic {

  def get (elem: String): Any = {
      schema.trace.info("getting value for property \"%s\" for oid %d".format(elem, oid))
      for (prop <- fileMap(oid) if prop.getName == elem) return prop.get
      return null
  } // get

  def set (elem: String) (value: Any): Any = {

      schema.trace.info("setting value for property \"%s\" = %s for oid %d".format(elem, value, oid))

      val row = fileMap(oid)

      // check to make sure the property exists
      for (prop <- row if prop.getName == elem) {
	
	// set the value
	val ret = prop.setFromAny(value)

	if (ret) {
	  // update the map
	  fileMap += oid -> row
	} // if

	return ret

      } // for

      return true
  } // set

  // allows us to get a property by name
  def applyDynamic (elem: String) (args: Any*): Any = {

    if (elem.startsWith("get")) {

      val pName = elem.substring(3, 4).toLowerCase + elem.replaceAllLiterally("get", "").substring(1)
      return get(pName)
      
    } else if (elem.startsWith("set")) {

      val pName = elem.substring(3, 4).toLowerCase + elem.replaceAllLiterally("set", "").substring(1)
      return set(pName)(args(0))
      
    } // if

    schema.trace.warning("thought you wanted a row property called \"%s\", but it does not exist".format(elem))

    // operation doesn't exist
    throw new UnsupportedOperationException

  } // selectDynamic

} // SchemaRow

object SchemaTest extends App {

  val db = new Database("MyTestDB")

  class PersonSchema () extends Schema ("person", db) {
    register(IntProperty("age"))
    register(StringProperty("name", 32))
  } // PersonSchema

  db.registerSchema(new PersonSchema())

//  db.person(10).setAge(25)
//  db.person(10).setName("Michael")

  println("age of oid %d = %s".format(10, db.person(10).getAge))
  println("name of oid %d = %s".format(10, db.table("person")(10).get("name")))

} // SchemaTest
