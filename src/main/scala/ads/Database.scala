package ads

import ads.concurrency._

import akka.actor.{ Actor, ActorContext, ActorSystem, Props, TypedActor, TypedProps }
import akka.event.{ Logging, LogSource }

/**
 * Companion object for Database class
 */
object Database {

  implicit val logSource: LogSource[AnyRef] = new LogSource[AnyRef] {
    def genString(o: AnyRef): String = "Database(\"%s\")".format(o.asInstanceOf[Database].name)
    override def getClazz(o: AnyRef): Class[_] = o.getClass
  }

} // Database

class Database (val name: String) {

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

} // Database
