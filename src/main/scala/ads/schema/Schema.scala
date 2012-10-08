package ads.schema

import scala.collection.mutable.{ HashMap, Map }

import akka.event.{ Logging, LogSource }
import akka.actor.ActorSystem

import ads.Database

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

  // implicit for this schema allows automatic registration of properties
  implicit val schema = this

  // akka logger
  private val trace = Logging(db.system, this)

  // properties list
  val properties: Map[String, Property[_]] = new HashMap[String, Property[_]]()

  // register a property with this schema
  def register (p: Property[_]): Unit = {

    if (properties.contains(p.getName)) {
      trace.warning("Tried adding a duplicate property to a schema. Property name = \"%s\"".format(this, p.getName))
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
    println(" - db: %s".format(db))
    println(" - row byte width: %d".format(width))
    println(" - properties:")
    for (p <- properties.keys) println("    - %s -> %s".format(p, properties(p)))
  } // printSchema

  def getPropArray: Array[Property[_]] = properties.values.map(_.makeClone).toArray

} // Schema

object SchemaTest extends App {

  val db = new Database("TestDB")

  class MySchema () extends Schema ("MySchema", db) {

    val id   = IntProperty("id")
    val name = StringProperty("name", 32)

  } // MySchema

  val schema = new MySchema()

  schema.printSchema

} // SchemaTest
