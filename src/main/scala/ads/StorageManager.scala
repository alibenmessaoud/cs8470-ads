package ads

import ads.util.FileMap

class StorageManager (db: Database) {

  def read (oid: Int): Array[Any] = null

  def write (oid: Int, value: Array[Any]) { }

  def flush () { }

}
