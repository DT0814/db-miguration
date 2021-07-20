package bbst

import java.io.{File, FileInputStream}
import java.security.MessageDigest

import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source


object BootStart {
  val log: Logger = LoggerFactory.getLogger("BootStart")

  def md5(s: String) = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }

  def initialize(migrationRepository: MigrationRepository) {
    migrationRepository.execute(
      "CREATE TABLE IF NOT EXISTS migration_process" +
        "(" +
        "    id   INTEGER PRIMARY KEY," +
        "    name VARCHAR(255) NOT NULL," +
        "    hash  VARCHAR(255) NOT NULL" +
        ");")
  }

  def isAllDigits(x: String): Boolean = x forall Character.isDigit

  def checkMigrationName(migrationName: String): Boolean = {
    val migrationNameSplit: Array[String] = migrationName.split('_')
    if (migrationNameSplit.length < 2) {
      return false
    }
    val migrationVersion: String = migrationNameSplit(0)
    if (!isAllDigits(migrationVersion)) {
      return false
    }
    true
  }

  def checkMigrationVersion(
                             migrationVersion: Integer,
                             migrationHash: String,
                             migrationRepository: MigrationRepository
                           ): Boolean = {
    val migrationProcessList = migrationRepository.findByIdLessThenAndLimit(migrationVersion, 1)

    if (migrationProcessList.nonEmpty && migrationVersion != migrationProcessList(0).id + 1) {
      log.error("migration version only supports incrementing one step.")
      return false
    }

    if (migrationProcessList.isEmpty && migrationVersion != 1) {
      log.error("the first migration version is must be 1.")
      return false
    }

    true
  }

  def checkMigrationVersionRunFinished(
                                        migrationVersion: Integer,
                                        migrationHash: String,
                                        migrationRepository: MigrationRepository
                                      ): Boolean = {
    val optionMigration = migrationRepository.findById(migrationVersion)
    optionMigration match {
      case Some(migration) =>
        if (migration.hash != migrationHash) {
          throw CanNotSupportMigrationChangeException("can not support change migration when it finish run.")
        }
        else
          true
      case None =>
        false
    }
  }

  def main(args: Array[String]): Unit = {

    val connection = new JdbcConnectionUtils().getConnection
    val migrationRepository = new MigrationRepository(connection)

    initialize(migrationRepository)

    val path = getClass.getResource("/migrations")
    val folder = new File(path.getPath)

    folder.listFiles().toList.foreach(file => {
      val migrationName = file.getName
      if (!checkMigrationName(migrationName)) {
        log.error(s"invalid migration file name${migrationName}")
        return
      }
      val migrationVersion = Integer.valueOf(migrationName.split('_')(0))
      val sql = Source.fromInputStream(new FileInputStream(file)).mkString

      if (!checkMigrationVersionRunFinished(migrationVersion, md5(sql), migrationRepository)) {
        log.info(s"run sql ${migrationName}\n $sql")
        if (!checkMigrationVersion(migrationVersion, md5(sql), migrationRepository)) {
          log.error(s"invalid migration file version${migrationName}")
          throw
            return
        }
        val migrationSql = sql.appendedAll(s"insert into migration_process(id,name,hash) values($migrationVersion,'$migrationName','${md5(sql)}')")
        log.debug(s"run migrationSql\n $migrationSql")
        val bool = migrationRepository.execute(migrationSql)
        if (!bool) {
          return
        }
      } else {
        return
      }
    })

  }
}


