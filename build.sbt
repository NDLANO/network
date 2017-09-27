val Scalaversion = "2.12.1"
val CrossScalaVersions = "2.11.8"
val ScalaTestVersion = "3.0.1"
val MockitoVersion = "1.10.19"
val AwsSdkversion = "1.11.93"
val ScalaLoggingVersion = "3.5.0"
val Json4sVersion = "3.5.0"

lazy val commonSettings = Seq(
  organization := "ndla",
  scalaVersion := Scalaversion,
  crossScalaVersions := Seq(CrossScalaVersions, Scalaversion)
)

lazy val network = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "network",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions := Seq("-target:jvm-1.8"),
    libraryDependencies ++= Seq(
      "org.json4s"   %% "json4s-jackson" % Json4sVersion,
      "org.json4s"   %% "json4s-native" % Json4sVersion,
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "org.mockito" % "mockito-all" % MockitoVersion % "test",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided;test",
      "com.amazonaws" % "aws-java-sdk-s3" % AwsSdkversion,
      "com.pauldijou" %% "jwt-json4s-native" % "0.14.0")
  )

publishTo := {
  val nexus = sys.props.getOrElse("nexus.host", "")
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/ndla-snapshots")
  else
    Some("releases"  at nexus + "content/repositories/ndla-releases")
}