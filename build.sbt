val scala213 = "2.13.3"
val scala212 = "2.12.10"
val Scalaversion = scala213

val ScalaTestVersion = "3.2.1"
val MockitoVersion = "1.14.8"
val AwsSdkversion = "1.11.438"
val Json4sVersion = "3.6.7"
val JacksonVersion = "2.12.1"

lazy val supportedScalaVersions = List(
  scala213,
  scala212
)

lazy val commonSettings = Seq(
  organization := "ndla",
  scalaVersion := Scalaversion,
  crossScalaVersions := supportedScalaVersions
)

// Workaround for: https://github.com/sbt/sbt/issues/3570
updateOptions := updateOptions.value.withGigahorse(false)

// Sometimes we override transitive dependencies because of vulnerabilities, we put these here
val vulnerabilityOverrides = Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % JacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
  "commons-codec" % "commons-codec" % "1.14",
  "org.apache.httpcomponents" % "httpclient" % "4.5.13"
)

lazy val network = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "network",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions := Seq("-target:jvm-1.8", "-deprecation"),
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-jackson" % Json4sVersion,
      "org.json4s" %% "json4s-native" % Json4sVersion,
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "org.mockito" %% "mockito-scala" % MockitoVersion % "test",
      "org.mockito" %% "mockito-scala-scalatest" % MockitoVersion % "test",
      "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided;test",
      "com.amazonaws" % "aws-java-sdk-s3" % AwsSdkversion,
      "com.pauldijou" %% "jwt-json4s-native" % "4.3.0"
    ) ++ vulnerabilityOverrides
  )

val checkfmt = taskKey[Boolean]("Check for code style errors")
checkfmt := {
  val noErrorsInMainFiles = (Compile / scalafmtCheck).value
  val noErrorsInTestFiles = (Test / scalafmtCheck).value
  val noErrorsInSbtConfigFiles = (Compile / scalafmtSbtCheck).value

  noErrorsInMainFiles && noErrorsInTestFiles && noErrorsInSbtConfigFiles
}

Test / test := (Test / test).dependsOn(Test / checkfmt).value

val fmt = taskKey[Unit]("Automatically apply code style fixes")
fmt := {
  (Compile / scalafmt).value
  (Test / scalafmt).value
  (Compile / scalafmtSbt).value
}

publishTo := {
  val nexus = sys.props.getOrElse("nexus.host", "")
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/ndla-snapshots")
  else
    Some("releases" at nexus + "content/repositories/ndla-releases")
}
