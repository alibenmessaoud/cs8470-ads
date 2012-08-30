class Page (n: Int) {

  var accessed = 0
  var modified = false
  var dirty    = false

  val contents = Array.ofDim[Record](n)

} // Page
