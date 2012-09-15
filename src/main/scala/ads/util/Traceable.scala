package ads
package util

object Level extends Enumeration {
  type Level = Value
  val Severe, Warning, Info, Config, Fine, Finer, Finest = Value
} // Op

trait Traceable [T] { self =>
  
  protected var TRACE = false
  
  def trace (msg:String)(implicit m : ClassManifest[T]) = if (TRACE) {
    println("%d - %s [%s] - %s".format(System.nanoTime, m.erasure, Level.Info, msg))
  } // trace
  
  def trace (lvl: Level.Level, msg:String)(implicit m : ClassManifest[T]) = if (TRACE) {
    println("%d - %s [%s] - %s".format(System.nanoTime, m.erasure, lvl, msg))
  } // trace
  
} // Traceable