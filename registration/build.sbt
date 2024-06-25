ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"


libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.10.0",
  "org.typelevel" %% "cats-effect" % "3.5.4",
  "org.mindrot" % "jbcrypt" % "0.4",

  "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC2",
  "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2",
)

scalacOptions ++= Seq(
  "-new-syntax",
  "-Xfatal-warnings",
  "-deprecation"
)

lazy val root = (project in file("."))
  .settings(
    name := "registration"
  )
