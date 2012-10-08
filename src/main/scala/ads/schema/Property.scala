package ads.schema

/**
 * Companion object for Property class
 */
object Property {

  /*
   * Byte arrays generated from properties must be multiples of theses numbers
   * in length.
   */

  val INT_BYTES_WIDTH    = 4
  val LONG_BYTES_WIDTH   = 8

  val FLOAT_BYTES_WIDTH  = 4
  val DOUBLE_BYTES_WIDTH = 8
 
} // Property

abstract class Property [T] (name: String, default: T, required: Boolean = false, index: Boolean = false, validator: T => Boolean = (e: T) => true) 
                            (implicit schema: Schema) {

  // register the property with the schema
  schema.register(this)

  /**
   * Holds the current value of the Property
   */
  private var value: T = default

  /**
   * The maximum byte width of this property
   */
  def width: Int

  /**
   * Return the current value of the Property
   */
  def get: T = value

  /**
   * Sets the current value of the Property. The return value of this function
   * is the same as the validator function for this property. If a custom
   * validator is attached to this Property then the value will change only if
   * the validator function returns true. The default validator function
   * returns true.
   *
   * @param value the value that this property should be set to
   * @return the value returned by appyling the validator function to the input
   *         parameter.
   */
  def set (value: T): Boolean = if(validator(value)) {
    this.value = value
    true
  } else {
    false
  } // set

  /**
   * Returns the byte array representation for this property's value
   *
   * @return a byte array representation of this property's value
   */
  def getAsByteArray: Array[Byte]

  /**
   * Returns a property generated from a byte array.
   *
   * @param bytes the byte array
   * @return a property generated from the byte array
   */
  def setFromByteArray [T] (bytes: Array[Byte]): Boolean

  def getName = name
  def isRequired = required

} // Property

