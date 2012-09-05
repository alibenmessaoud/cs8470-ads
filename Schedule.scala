
/***********************************************************************************
 * @author  John Miller
 * @version 1.0
 * @date    Wed Feb 10 12:46:34 EST 2010
 */

/***********************************************************************************
 * This trait defines read (r) and write (w) operation-types.
 */
trait ReadWrite
{
    val r = 'r'
    val w = 'w'

} // ReadWrite

/***********************************************************************************
 * This class represents a transaction schedule as a list of operations, where each
 * operation is a 3-tuple (operation-type, transaction-id, data-object-id).
 */
class Schedule (s: List [Tuple3 [Char, Int, Int]]) extends ReadWrite
{
    /*******************************************************************************
     * Determine whether this schedule is Conflict Serializable (CSR).
     * @param nTrans  the number of transactions in this schedule
     */
    def isCSR (nTrans: Int): Boolean =
    {
        false
    } // isCSR

    /*******************************************************************************
     * Determine whether this schedule is View Serializable (VSR).
     * @param nTrans  the number of transactions in this schedule
     */
    def isVSR (nTrans: Int): Boolean =
    {
        false
    } // isVSR

    /*******************************************************************************
     * Randomly generate a schedule.
     * @param nTrans  the number of transactions
     * @param nOps    the number of operations per transaction
     * @param nObjs   the number of data objects accessed
     */
    def genSchedule (nTrans: Int, nOps: Int, nObjs: Int): Schedule =
    {
        null
    } // genSchedule

    /*******************************************************************************
     * Iterate over the schedule element by element.
     * @param f  the function to apply
     */
    def foreach [U] (f: Tuple3 [Char, Int, Int] => U)
    {
        s.foreach (f)
    } // foreach

    /*******************************************************************************
     * Convert the schedule to a string.
     */
    override def toString: String =
    {
        "Schedule ( " + s + " )"
    } // toString:

} // Schedule class

/***********************************************************************************
 * This object is used to test the Schedule class.
 */
object ScheduleTest extends Application with ReadWrite
{
    val s1 = new Schedule (List ( (r, 0, 0), (r, 1, 0), (w, 0, 0), (w, 1, 0) ))
    val s2 = new Schedule (List ( (r, 0, 0), (r, 1, 1), (w, 0, 0), (w, 1, 1) ))

    println ("s1 = " + s1 + " is CSR? " + s1.isCSR (2))
    println ("s1 = " + s1 + " is VSR? " + s1.isVSR (2))
    println ("s2 = " + s2 + " is CSR? " + s1.isCSR (2))
    println ("s2 = " + s2 + " is VSR? " + s1.isVSR (2))

} // ScheduleTest object

