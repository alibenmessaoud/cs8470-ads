
/***********************************************************************************
 * @author  John Miller
 * @version 1.0
 * @date    Thu Feb 11 12:38:17 EST 2010
 */

/***********************************************************************************
 * Abstract representation of a database as a collection of data objects and a
 * lock table.
 */
object Database
{
    /** The lock table to control concurrent access to the database
     */
    val lt   = new LockTable ()

    /** The abstract database representation (an array of double objects)
     */
    val data = new Array [Double] (100)

} // Database object

/***********************************************************************************
 * This class models database transactions.
 * @param tid  the transaction identifier
 * @param s    the schedule of read/write operations making up the transaction
 */
class Transaction (tid: Int, s: Schedule) extends Thread with ReadWrite
{
    /*******************************************************************************
     * Run the transaction: begin, reads/writes, commit.
     */
    override def run ()
    {
        var value = 0.
        begin ()
        for (op <- s) {
            if (op._1 == r) {
                Database.lt.rl (tid, op._3); value = read (op._3)
            } else {
                Database.lt.wl (tid, op._3); write (op._3, value + 1.)
            } // if
        } // for
        for (op <- s) Database.lt.ul (op._2, op._3)
        commit ()
    } // run

    /*******************************************************************************
     * Begin this transaction.
     */
    def begin ()
    {
        Thread.sleep (5)
        println ("begin transaction " + tid)
    } // begin

    /*******************************************************************************
     * Read data object oid.
     * @param oid  the database object
     */
    def read (oid: Int): Double =
    {
        Thread.sleep (10)
        val value = Database.data(oid) 
        println ("read " + tid + " ( " + oid + " ) value = " + value)
        value
    } // read

    /*******************************************************************************
     * Write data object oid.
     * @param oid  the database object
     */
    def write (oid: Int, value: Double)
    {
        Thread.sleep (15)
        println ("write " + tid + " ( " + oid + " ) value = " + value)
        Database.data(oid) = value
    } // write

    /*******************************************************************************
     * Commit this transaction.
     */
    def commit ()
    {
        Thread.sleep (20)
        println ("commit transaction " + tid)
    } // commit

} // Transaction class

/***********************************************************************************
 * Test the Transcation class by running several concurrent transactions/threads.
 */
object TransactionTest extends Application with ReadWrite
{
    println ("Test Transactions")
    val t1 = new Transaction (1, new Schedule (List ( (w, 1, 0), (w, 1, 1) )))
    val t2 = new Transaction (2, new Schedule (List ( (w, 2, 0), (w, 2, 1) )))

    t1.start ()
    t2.start ()

} // TransactionTest object

