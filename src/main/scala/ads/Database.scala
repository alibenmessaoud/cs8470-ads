package ads

import scala.collection.mutable.{ Map, HashMap }

import akka.actor.{ Actor, ActorContext, ActorSystem, Props, TypedActor, TypedProps }
import akka.event.{ Logging, LogSource }

import ads.concurrency._
import ads.schema.{ Schema, SchemaRow }

/**
 * Companion object for Database class
 */
object Database {

  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = "Database(\"%s\")".format(o.asInstanceOf[Database].name)
    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

} // Database

class Database (val name: String) extends Dynamic {

  /**
   * The table schemas associated with this database
   */
  val schemas: Map[String, Schema] = new HashMap[String, Schema]()

  def registerSchema (schema: Schema): Unit = {
    schemas += schema.getName -> schema
    trace.info("registered %s".format(schema))
  } // registerScheme

  /**
   * This Database's ActorSystem.
   */
  val system = ActorSystem("Database-%s".format(name))

  /**
   * This database's TransactionManager as an Actor
   */
//  val tm = system.actorOf(Props(new TransactionManager() with SGC), name = "TransactionManager")
  val tm = system.actorOf(Props(new TransactionManager() with SGC), name = "TransactionManager")

  /**
   * Logger for this database
   */
  val trace = Logging(system, this)

  // go ahead and log some stuff
  trace.info("%s started".format(this))

  /**
   * Creates a Transaction for this database based on an implementation of the
   * Transaction trait.
   *
   * @param impl an implementation of the Transaction trait
   * @param name the name of the transaction
   */
  def makeTransaction (impl: Transaction, name: String) = 
    TypedActor(system).typedActorOf(TypedProps(classOf[Transaction], impl), "Transaction-%s".format(name))

  /**
   * Creates and executes a Transaction for this database based on an
   * implementation of the Transaction trait.
   *
   * @param impl an implementation of the Transaction trait
   * @param name the name of the transaction
   */
  def makeAndExecTransaction (impl: Transaction, name: String) = 
    makeTransaction(impl, name).execute

  /**
   * Return a table by name
   *
   * @param tbl the name of the table
   * @return the table schema
   */
  def table (tbl: String): Schema = {

    // if there is a table schema by that name then return it
    if (schemas.contains(tbl)) return schemas(tbl)

    trace.warning("thought you wanted a table called \"%s\", but it does not exist".format(tbl))
    null

  } // table

  /**
   * Allows us to grab table schemas as if they were methods
   */
  def applyDynamic (methodName: String) (args: Any*) : SchemaRow = {

    // if there is a table schema by that name then return it
    if (schemas.contains(methodName)) return table(methodName)(args(0).asInstanceOf[Int])

    trace.warning("thought you wanted a table called \"%s\", but it does not exist".format(methodName))

    // operation doesn't exist
    throw new UnsupportedOperationException

  } // selectDynamic

  override def toString = "Database(\"%s\")".format(name)

} // Database
