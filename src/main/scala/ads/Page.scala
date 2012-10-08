package ads

import scala.collection.mutable.{ HashMap, Map }

class Page (val pid: Int) {

  var accessed = System.currentTimeMillis // last time of access
  var modified = false // change to the page
  var dirty    = false // data written, but not yet comitted.

} // Page

