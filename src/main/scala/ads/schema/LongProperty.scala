package ads.schema

import java.nio.ByteBuffer

import ads.Database

case class LongProperty (name: String, default: Long = 0L, required: Boolean = false, index: Boolean = false, validator: Long => Boolean = (e: Long) => true) 
                        (implicit schema: Schema) extends Property [Long] (name, default, required, index, validator) (schema) {

  def width = Property.LONG_BYTES_WIDTH

  def makeClone [LongProperty] = {
    val obj = LongProperty(name, default, required, index, validator)
    obj.set(get)
    obj.asInstanceOf[LongProperty]
  } // clone

  def getAsByteArray: Array[Byte] = {

    // create the byte buffer 
    val byteBuffer = ByteBuffer.allocate(width)

    // add the number to the buffer
    byteBuffer.putLong(get)

    // return the bytes array
    byteBuffer.array

  } // toByteArray

  def setFromByteArray [String] (bytes: Array[Byte]): Boolean = {

    // create the byte buffer
    val byteBuffer = ByteBuffer.wrap(bytes)

    // get the integer value from this byte buffer
    val i = byteBuffer.getLong

    if (validator(i)) {
      set(i)
      true
    } else {
      false
    } // if

  } // setFromByteArray

  override def toString = "LongProperty(%s = \"%s\")".format(name, getName)

} // LongProperty

object LongPropertyTest extends App {

  val db = new Database("TestDB")
  implicit val s = new Schema("test", db)

  val p = new LongProperty("p")

  p.set(492L)

  println(p.get)
  println(p.getAsByteArray.deep)

  val bytes = Array(0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 0.toByte, 2.toByte, (-20).toByte)

  p.setFromByteArray(bytes)
  println(p.get)

  p.set(748L)

  println(p.get)
  println(p.getAsByteArray.deep)
  

} // LongPropertyTest

