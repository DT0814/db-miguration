package bbst

import java.sql.{Connection, DriverManager}


class JdbcConnectionUtils {

  def getConnection: Connection = {
    try {
      classOf[org.postgresql.Driver]
      val url = "jdbc:postgresql://localhost:5432/test_db?user=admin&password=admin"
      return DriverManager.getConnection(url)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    throw FailedToCreateJdbcConnection();
  }
}
