name := "jefe"
organization in ThisBuild := "com.outr"
version in ThisBuild := "1.0.0"
scalaVersion in ThisBuild := "2.12.1"
crossScalaVersions in ThisBuild := List("2.12.1", "2.11.8")
sbtVersion in ThisBuild := "0.13.13"
resolvers in ThisBuild ++= Seq(
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

val asmVersion = "5.2"
val coursierVersion = "1.0.0-M15-4"
val packrVersion = "2.1"
val powerScalaVersion = "2.0.5"
val proguardVersion = "5.3.2"
val scalaXMLVersion = "1.0.6"
val scribeVersion = "1.4.1"

val reactifyVersion = "1.4.5-SNAPSHOT"
val youiVersion = "0.2.2-SNAPSHOT"

lazy val root = project.in(file("."))
  .aggregate(launch, manager, runner, optimizer, pack, server, consoleJVM, consoleJS, example)
  .settings(
    publishArtifact := false
  )

lazy val launch = project.in(file("launch"))
  .settings(
    name := "jefe-launch",
    libraryDependencies ++= Seq(
      "com.outr" %% "reactify" % reactifyVersion,
      "com.outr" %% "scribe-slf4j" % scribeVersion
    )
  )

lazy val manager = project.in(file("manager"))
  .settings(
    name := "jefe-manager",
    libraryDependencies ++= Seq(
      "io.get-coursier" %% "coursier" % coursierVersion,
      "io.get-coursier" %% "coursier-cache" % coursierVersion,
      "org.powerscala" %% "powerscala-core" % powerScalaVersion,
      "org.powerscala" %% "powerscala-io" % powerScalaVersion,
      "org.scala-lang.modules" %% "scala-xml" % scalaXMLVersion,
      "com.outr" %% "scribe-slf4j" % scribeVersion
    )
  )

lazy val runner = project.in(file("runner"))
  .settings(
    name := "jefe-runner",
    assemblyJarName in assembly := "runner.jar"
  )
  .dependsOn(launch, manager)

lazy val optimizer = project.in(file("optimizer"))
  .settings(
    name := "jefe-optimizer",
    libraryDependencies ++= Seq(
      "org.powerscala" %% "powerscala-core" % powerScalaVersion,
      "org.powerscala" %% "powerscala-io" % powerScalaVersion,
      "com.outr" %% "scribe-slf4j" % scribeVersion,
      "org.ow2.asm" % "asm" % asmVersion
    )
  )

lazy val pack = project.in(file("pack"))
  .settings(
    name := "jefe-pack",
    libraryDependencies ++= Seq(
      "com.bladecoder.packr" % "packr" % packrVersion,
      "net.sf.proguard" % "proguard-base" % proguardVersion
    )
  )
  .dependsOn(runner, optimizer)

lazy val server = project.in(file("server"))
  .settings(
    name := "jefe-server",
    assemblyJarName := s"${name.value}-${version.value}.jar",
    libraryDependencies ++= Seq(
      "io.youi" %% "youi-server-undertow" % youiVersion,
      "org.powerscala" %% "powerscala-command" % powerScalaVersion,
      "org.powerscala" %% "powerscala-concurrent" % powerScalaVersion
    )
  )
  .dependsOn(runner)

lazy val console = crossProject.in(file("console"))
  .settings(
    name := "jefe-console",
    libraryDependencies ++= Seq(
      "io.youi" %%% "youi-app" % youiVersion
    )
  )
  .jsSettings(
    crossTarget in fastOptJS := baseDirectory.value / ".." / "jvm" / "src" / "main" / "resources" / "app",
    crossTarget in fullOptJS := baseDirectory.value / ".." / "jvm" / "src" / "main" / "resources" / "app"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "io.youi" %% "youi-server-undertow" % youiVersion
    )
  )
lazy val consoleJVM = console.jvm
lazy val consoleJS = console.js

lazy val example = project.in(file("example"))
  .settings(
    name := "jefe-example"
  )
  .dependsOn(launch, manager)