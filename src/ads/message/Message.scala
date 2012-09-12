package ads
package message

import Op._

/**
 * Represents a message.
 * 
 * @author Michael E. Cotterell
 * @author Terrance Medina	
 */
abstract class Message () { }

/**
 * A message idicating that a Transaction has begun.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param t A Transaction object.
 */
case class BeginMessage (t: Transaction) extends Message ()

/**
 * A message indicating that a Transaction requests to read a value from
 * the database.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param t A Transaction object.
 * @param oid An object identifier.
 */
case class ReadMessage (t: Transaction, oid: Int) extends Message ()

/**
 * A message indicating that a Transaction requests to write a value into
 * the database.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param t A Transaction object.
 * @param oid An object identifier.
 * @param value The value that is to be written.
 */
case class WriteMessage (t: Transaction, oid: Int, value: Any) extends Message ()

/**
 * A message indicating that a Transaction requests to commit itself to the
 * database.
 *
 * @author Michael E. Cotterell
 * @author Terrance Medina
 * @param t A Transaction object.
 */
case class CommitMessage (t: Transaction) extends Message ()
