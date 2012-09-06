class Page (n: Int) {

  var accessed = 0     // last time of access
  var modified = false // change to the page
  var dirty    = false // data written, but not yet comitted.

  val contents = Array.ofDim[Record](n)

} // Page

