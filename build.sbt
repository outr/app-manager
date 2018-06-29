name := "jefe"
organization in ThisBuild := "com.outr"
version in ThisBuild := "2.0.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.6"
crossScalaVersions in ThisBuild := List("2.12.6", "2.11.12")
resolvers in ThisBuild ++= Seq(
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")
cancelable in Global := true

publishTo in ThisBuild := sonatypePublishTo.value
sonatypeProfileName in ThisBuild := "com.outr"
publishMavenStyle in ThisBuild := true
licenses in ThisBuild := Seq("MIT" -> url("https://github.com/outr/jefe/blob/master/LICENSE"))
sonatypeProjectHosting in ThisBuild := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "jefe", "matt@outr.com"))
homepage in ThisBuild := Some(url("https://github.com/outr/jefe"))
scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/outr/jefe"),
    "scm:git@github.com:outr/jefe.git"
  )
)
developers in ThisBuild := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("http://matthicks.com"))
)

val coursierVersion = "1.0.3"
val libraryManagementVersion = "1.1.4"
val powerscalaVersion = "2.0.5"
val reactifyVersion = "2.3.0"
val scribeVersion = "2.5.1"

lazy val root = project.in(file("."))
  .aggregate(resolve, launch, application)
  .settings(
    publishArtifact := false
  )

lazy val resolve = project.in(file("resolve"))
  .settings(
    name := "jefe-resolve",
    libraryDependencies ++= Seq(
      "io.get-coursier" %% "coursier" % coursierVersion,
      "io.get-coursier" %% "coursier-cache" % coursierVersion,
      "org.scala-sbt" %% "librarymanagement-core" % libraryManagementVersion,
      "org.scala-sbt" %% "librarymanagement-ivy" % libraryManagementVersion,
      "org.powerscala" %% "powerscala-core" % powerscalaVersion,
      "org.powerscala" %% "powerscala-io" % powerscalaVersion,
      "com.outr" %% "scribe-slf4j" % scribeVersion
    )
  )

lazy val launch = project.in(file("launch"))
  .settings(
    name := "jefe-launch",
    libraryDependencies ++= Seq(
      "org.powerscala" %% "powerscala-core" % powerscalaVersion,
      "org.powerscala" %% "powerscala-concurrent" % powerscalaVersion,
      "com.outr" %% "scribe-slf4j" % scribeVersion
    )
  )

lazy val application = project.in(file("application"))
  .settings(
    name := "jefe-application",
    libraryDependencies ++= Seq(
      "com.outr" %% "reactify" % reactifyVersion
    )
  )
  .dependsOn(resolve, launch)