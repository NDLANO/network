val Scalaversion = "2.11.8"
val ScalaTestVersion = "2.2.4"
val MockitoVersion = "1.10.19"

lazy val commonSettings = Seq(
  organization := "ndla",
  scalaVersion := Scalaversion
)

lazy val network = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "network",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions := Seq("-target:jvm-1.7"),
    libraryDependencies ++= Seq(
      "org.json4s"   %% "json4s-jackson" % "3.3.0",
      "org.scalaj" %% "scalaj-http" % "1.1.5",
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "org.mockito" % "mockito-all" % MockitoVersion % "test",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided;test")
  )

publishTo := {
  val nexus = sys.props.getOrElse("nexus.host", "")
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/ndla-snapshots")
  else
    Some("releases"  at nexus + "content/repositories/ndla-releases")
}