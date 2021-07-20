package bbst

import java.sql.{Connection, ResultSet, Statement}

import scala.collection.mutable.ListBuffer

class MigrationRepository {

  def buildMigrationProcess(set: ResultSet): List[MigrationProcess] = {
    val list: ListBuffer[MigrationProcess] = new ListBuffer()
    while (set.next()) {
      list.addOne(MigrationProcess(
        set.getInt("id"),
        set.getString("name"),
        set.getString("hash")
      ))
    }
    list.toList
  }

  def findById(id: Integer): Option[MigrationProcess] = {
    var statement: Statement = null
    try {
      statement = connection.createStatement()
      val set = statement.executeQuery(s"select id,name,hash from migration_process where id = $id")
      val list: List[MigrationProcess] = buildMigrationProcess(set)
      if (list.size > 1) {
        throw SqlResultException("exception one row but return mutable rows")
      }
      if (list.size == 1)
        Some(list(0))
      else
        None
    } catch {
      case e: Exception =>
        e.printStackTrace()
        None
    }
  }

  def findByIdLessThenAndLimit(id: Integer, limit: Integer): List[MigrationProcess] = {
    var statement: Statement = null
    try {
      statement = connection.createStatement()
      val set = statement.executeQuery(s"select id,name,hash from migration_process where id < $id order by id desc limit $limit")
      buildMigrationProcess(set)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        throw e
    }
  }

  private var connection: Connection = _

  def this(connection: Connection) = {
    this()
    this.connection = connection
  }

  def execute(sql: String): Boolean = {
    connection.setAutoCommit(false)

    var statement: Statement = null
    try {
      statement = connection.createStatement()
      statement.execute(sql)
      connection.commit()
      true
    } catch {
      case e: Exception =>
        e.printStackTrace()
        connection.rollback()
        false
    } finally {
      statement.close()
    }
  }

}
