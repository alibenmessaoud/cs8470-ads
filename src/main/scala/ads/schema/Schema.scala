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
 * A Database Schema
 */
class Schema (val _name: String, val db: Database) {

  // implicit for this schema allows automatic registration of properties
  implicit val schema = this

  // akka logger
  private val trace = Logging(db.system, this)

  // properties list
  private val properties: Map[String, Property[_]] = new HashMap[String, Property[_]]()

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

  def width = properties.values.map(_.width).sum

  override def toString = "Schema(name = \"%s\", width = %d)".format(_name, width)

  def getName = _name

} // Schema

object SchemaTest extends App {

  val db = new Database("TestDB", null)

  class MySchema () extends Schema ("MySchema", db) {

    val id   = IntProperty("id")
    val name = StringProperty("name", 32)

  } // MySchema

  val schema = new MySchema()

  println(schema)

} // SchemaTest
