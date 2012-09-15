package ads.message

sealed trait CheckResponse { def name: String }

object CheckResponses {
  case object Granted    extends CheckResponse { val name = "Granted" }
  case object Denied     extends CheckResponse { val name = "Denied" }
  case object Postponed  extends CheckResponse { val name = "Postponed" }
  case object Rollbacked extends CheckResponse { val name = "Rollbacked" }
  case object Thomas     extends CheckResponse { val name = "Thomas" }
}

