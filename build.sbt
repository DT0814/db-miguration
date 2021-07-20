name := "db-migration"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= {
  Seq(
    "org.slf4j" % "slf4j-api" % "1.7.30",
    "org.slf4j" % "slf4j-simple" % "1.7.30",
    "org.postgresql" % "postgresql" % "9.3-1102-jdbc41"
  )
}
