package ads.util

case class Levenshtein (a: String, b: String) {

  private implicit def booleanToInt (b: Boolean) = if (b) 1 else 0

  private def distance (i: Int, j: Int): Int = (i, j) match {
    case (i, j) if (i == j && i == 0) => 0
    case (i, j) if (j == 0 && i  > 0) => i
    case (i, j) if (i == 0 && j  > 0) => j
    case (i, j)                       => List(
      distance(i - 1, j)    + 1,
      distance(i, j - 1)    + 1,
      distance(i - 1, j- 1) + (a(i) != b(j))
    ).min
  } // distance

  def distance: Int = distance(a.length - 1, b.length - 1)

  def print: Unit = {
    println("δ(\"%s\", \"%s\") = %d".format(a, b, distance))
  } // print

} // Levenshtein

object LevenshteinTest extends App {

  Levenshtein("employee", "employer").print
  Levenshtein("employee", "empID").print
  Levenshtein("car", "automobile").print

} // LevenshteinTest
