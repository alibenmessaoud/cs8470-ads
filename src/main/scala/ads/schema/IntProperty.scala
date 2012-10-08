package ads.schema

import java.nio.ByteBuffer

import ads.Database

case class IntProperty (name: String, default: Int = 0, required: Boolean = false, index: Boolean = false, validator: Int => Boolean = (e: Int) => true) 
                       (implicit schema: Schema) extends Property [Int] (name, default, required, index, validator) (schema) {

  def width = Property.INT_BYTES_WIDTH

  def getAsByteArray: Array[Byte] = {

    // create the byte buffer 
    val byteBuffer = ByteBuffer.allocate(width)

    // add the number to the buffer
    byteBuffer.putInt(get)

    // return the bytes array
    byteBuffer.array

  } // toByteArray

  def setFromByteArray [String] (bytes: Array[Byte]): Boolean = {

    // create the byte buffer
    val byteBuffer = ByteBuffer.wrap(bytes)

    // get the integer value from this byte buffer
    val i = byteBuffer.getInt

    if (validator(i)) {
      set(i)
      true
    } else {
      false
    } // if

  } // setFromByteArray

  override def toString = "IntProperty(%s = \"%s\")".format(name, getName)

} // IntProperty

object IntPropertyTest extends App {

  val db = new Database("TestDB", null)
  implicit val s = new Schema("test", db)

  val p = new IntProperty("p")

  p.set(492)

  println(p.get)
  println(p.getAsByteArray.deep)

  val bytes = Array(0.toByte, 0.toByte, 2.toByte, (-20).toByte)

  p.setFromByteArray(bytes)
  println(p.get)

  p.set(748)

  println(p.get)
  println(p.getAsByteArray.deep)
  

} // IntPropertyTest

