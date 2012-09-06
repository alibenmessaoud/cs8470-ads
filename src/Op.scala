object Op extends Enumeration {
  type Op = Value
  val Read, Write, SharedLock, ExclusiveLock, UpgradeLock, Unlock, Commit = Value
} // Op
