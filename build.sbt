val Scalaversion = "2.12.1"
val ScalaTestVersion = "3.0.1"
val MockitoVersion = "1.10.19"
val AwsSdkversion = "1.11.93"
val ScalaLoggingVersion = "3.5.0"
val Json4sVersion = "3.5.0"

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
      "org.json4s"   %% "json4s-jackson" % Json4sVersion,
      "org.json4s"   %% "json4s-native" % Json4sVersion,
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "org.mockito" % "mockito-all" % MockitoVersion % "test",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided;test",
      "com.amazonaws" % "aws-java-sdk-s3" % AwsSdkversion,
      "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5")
  )

publishTo := {
  val nexus = sys.props.getOrElse("nexus.host", "")
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/ndla-snapshots")
  else
    Some("releases"  at nexus + "content/repositories/ndla-releases")
}