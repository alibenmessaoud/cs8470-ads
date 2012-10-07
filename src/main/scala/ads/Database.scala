package ads

import ads.concurrency._

import akka.actor.{ Actor, ActorContext, ActorSystem, Props, TypedActor, TypedProps }

case class Database [CC <: ConcurrencyControl] (val name: String, private val tm: TransactionManager with CC) {

  /**
   * This Database's ActorSystem.
   */
  val system = ActorSystem("Database-%s".format(name))

  /**
   * This database's TransactionManager as an Actor
   */
  val manager = system.actorOf(Props(tm), name = "TransactionManager")

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
