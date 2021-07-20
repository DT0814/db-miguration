package bbst

case class SqlResultException(value: String) extends RuntimeException(value) {}
