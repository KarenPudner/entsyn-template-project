name := """template-project"""
organization := "uk.co.bbc"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

val akkaVersion = "2.4.11.2"

libraryDependencies += filters
libraryDependencies ++= Seq(
  specs2 % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" % "akka-http-core_2.11" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaVersion,
  "mysql" % "mysql-connector-java" % "6.0.6",
  "org.scalikejdbc" %% "scalikejdbc"                  % "2.5.2",
  "org.scalikejdbc" %% "scalikejdbc-config"           % "2.5.2",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.5.1",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "xmlunit" % "xmlunit" % "1.6" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.6.0" % "test"
)


// Native Packager
enablePlugins(JavaServerAppPackaging)
enablePlugins(SystemVPlugin)

packageName := "template-project-package"
rpmVendor := "BBC"
rpmRelease := Option(System.getenv("BUILD_RELEASE")) getOrElse "1"
rpmLicense := Some("Copyright BBC MMXVII")
rpmGroup := Some("BBC")
rpmRequirements ++= Seq("cloud-httpd24-ssl-services-devs", "java-1.8.0-openjdk", "cosmos_to_env", "cloudteam-td-agent-setup")
packageDescription := "An example scala play project"

// Disable auto start for Cosmos packaging
serviceAutostart in Rpm := false

testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "exclude", "integration, database")

mappings in Universal += {
  baseDirectory.value / "cosmos" / "00default-vars.conf" -> "/httpd/00default-vars.conf"
}

mappings in Universal += {
  baseDirectory.value / "cosmos" / "move_files.sh" -> "move_files.sh"
}

// Add the contents of the bake-scripts directory into the RPM
mappings in Universal ++= {
  val bakeDir = baseDirectory.value / "cosmos" / "bake-scripts"

  for {
    (file, relativePath) <- (bakeDir.*** --- bakeDir) pair relativeTo(bakeDir)
  } yield file -> s"/etc/bake-scripts/$relativePath"
}

import com.typesafe.sbt.packager.rpm.RpmPlugin.Names.Post

maintainerScripts in Rpm := maintainerScriptsAppend((maintainerScripts in Rpm).value)(
  Post -> s"/usr/share/template-project-package/move_files.sh"
)

javaOptions in Universal ++= Seq(
  // Since play uses separate pidfile we have to provide it with a proper path
  // name of the pid file must be play.pid
  s"-Dpidfile.path=/var/run/${packageName.value}/play.pid"
)



