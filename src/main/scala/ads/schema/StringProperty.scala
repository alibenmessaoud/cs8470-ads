package ads.schema

import scala.collection.mutable.StringBuilder

import java.nio.{ ByteBuffer, CharBuffer }
import java.nio.charset.Charset

case class StringProperty (name: String, default: String = "", required: Boolean = false, index: Boolean = false, validator: String => Boolean = (e: String) => true) 
                          (implicit schema: Schema) extends Property [String] (name, default, required, index, validator) (schema) {

  private val encoding = "UTF-8"

  private def encoder = Charset.forName(encoding).newEncoder

  private def decoder = Charset.forName(encoding).newDecoder

  def getAsByteArray: Array[Byte] = {

    // generate a byte array with proper encoding
    val charBuffer = CharBuffer.wrap(get)
    val byteBuffer = encoder.encode(charBuffer)
    val bytes      = byteBuffer.array.slice(byteBuffer.position, byteBuffer.limit)

    // calculate and create the padding
    val padLength = Property.STRING_BYTES_WIDTH - (bytes.length % Property.STRING_BYTES_WIDTH)
    val padding = Array.ofDim[Byte](padLength)

    // return the padded array
    padding ++ bytes

  } // toByteArray

  def setFromByteArray [String] (bytes: Array[Byte]): Boolean = {

    // convert from bytes
    val byteBuffer = ByteBuffer.wrap(bytes)
    val charBuffer = decoder.decode(byteBuffer)
    val str        = charBuffer.toString

    if (validator(str)) {
      set(str)
      true
    } else {
      false
    } // if

  } // setFromByteArray

  override def toString = "StringProperty(%s = \"%s\")".format(name, get)

} // StringProperty

object StringPropertyTest extends App {

  implicit val s = new Schema("test")

  val p = new StringProperty("p")

  p.set("michael")

  println(p.get)
  println(p.getAsByteArray.deep)

  p.setFromByteArray(Array( 0.toByte, 0.toByte, 109.toByte, 105.toByte, 99.toByte, 104.toByte, 97.toByte, 101.toByte, 108.toByte, 32.toByte, 99.toByte))

  println(p.get)

} // StringPropertyTest
