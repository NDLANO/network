val Scalaversion = "2.13.0"
val ScalaTestVersion = "3.0.8"
val MockitoVersion = "2.23.0"
val AwsSdkversion = "1.11.438"
val Json4sVersion = "3.6.7"
val JacksonVersion = "2.9.9.3"

lazy val commonSettings = Seq(
  organization := "ndla",
  scalaVersion := Scalaversion
)

// Workaround for: https://github.com/sbt/sbt/issues/3570
updateOptions := updateOptions.value.withGigahorse(false)

lazy val network = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "network",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions := Seq("-target:jvm-1.8", "-deprecation"),
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion, // Overriding jackson-databind used in dependencies because of https://app.snyk.io/vuln/SNYK-JAVA-COMFASTERXMLJACKSONCORE-72884
      "org.json4s" %% "json4s-jackson" % Json4sVersion,
      "org.json4s" %% "json4s-native" % Json4sVersion,
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "org.mockito" % "mockito-core" % MockitoVersion % "test",
      "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided;test",
      "com.amazonaws" % "aws-java-sdk-s3" % AwsSdkversion,
      "com.pauldijou" %% "jwt-json4s-native" % "4.0.0",
      "org.bouncycastle" % "bcprov-jdk15on" % "1.60" // Overriding bouncycastle used in jwt-json4s-native: https://app.snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-32412
    )
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
