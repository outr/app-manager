package com.outr.jefe

import java.io.File
import java.nio.file.{Files, Path, Paths}
import scala.collection.JavaConverters._

object JefeNative {
  private val CheckVersionTimeout: Long = 24 * 60 * 60 * 1000
  private val MavenMetadataURL = "http://repo1.maven.org/maven2/com/outr/jefe-boot_2.12/maven-metadata.xml"

  // TODO: support other paths
  private lazy val curl = new File("/usr/bin/curl")
  private lazy val wget = new File("/usr/bin/wget")
  private lazy val nohup = new File("/usr/bin/nohup")

  // Determine the location of JRE
  private lazy val javaHome = Paths.get(determineJavaHome())
  private lazy val javaBin = javaHome.resolve("bin")
  private lazy val java = {
    val binJava = new File("/usr/bin/java")
    if (binJava.exists()) {
      binJava.toPath
    } else {
      javaBin.resolve("java")
    }
  }

  def main(args: Array[String]): Unit = {
    // Determine the home directory for Jefe
    val userHome = Paths.get(System.getProperty("user.home"))
    val directory = userHome.resolve(".jefe")
    Files.createDirectories(directory)

    // Determine the latest version of Jefe from Maven Central
    val latestVersionFile = directory.resolve("latest.version")
    val latestVersion = if (!Files.isRegularFile(latestVersionFile) || System.currentTimeMillis() - Files.getLastModifiedTime(latestVersionFile).toMillis > CheckVersionTimeout) {
      // Download latest version information
      val ReleaseRegex = """.*[<]release[>](.+)[<][/]release[>]""".r
      val version = linesFromMaven().collectFirst {
        case ReleaseRegex(v) => v
      }.getOrElse(throw new RuntimeException(s"Unable to find release in $MavenMetadataURL"))
      Files.write(latestVersionFile, version.getBytes("UTF-8"))
      version.trim
    } else {
      // Use cached version
      new String(Files.readAllBytes(latestVersionFile), "UTF-8").trim
    }

    // Download the latest assembly JAR if not already available
    val download = directory.resolve("download")
    Files.createDirectories(download)
    val assemblyJAR = download.resolve(s"jefe-boot-assembly-$latestVersion.jar")
    val assemblyJARTemp = download.resolve(s"jefe-boot-assembly-$latestVersion.jar.tmp")
    if (Files.notExists(assemblyJAR)) {
      Files.deleteIfExists(assemblyJARTemp)
      val jar = s"http://repo1.maven.org/maven2/com/outr/jefe-boot_2.12/$latestVersion/jefe-boot_2.12-$latestVersion-assembly.jar"
      println(s"Downloading $jar...")
      saveURL(jar, assemblyJARTemp)
      Files.move(assemblyJARTemp, assemblyJAR)
    }

    // Run the jar
    run(assemblyJAR, args.toList)
  }

  private def linesFromMaven(): List[String] = {
    val tempPath = Files.createTempFile("maven", "metadata")
    try {
      saveURL(MavenMetadataURL, tempPath)
      Files.lines(tempPath).iterator().asScala.toList.map(_.trim)
    } finally {
      Files.deleteIfExists(tempPath)
    }
  }

  private def determineJavaHome(): String = Option(System.getenv("JAVA_HOME")) match {
    case Some(home) => home
    case None => throw new RuntimeException(s"No JAVA_HOME environment variable set")
  }

  private def saveURL(url: String, output: Path): Unit = {
    val outputPath = output.toFile.getAbsolutePath
    val pb = if (wget.exists()) {
      new ProcessBuilder(wget.getAbsolutePath, "-O", outputPath, url)
    } else if (curl.exists()) {
      new ProcessBuilder(curl.getAbsolutePath, "--output", outputPath, url)
    } else {
      throw new RuntimeException("Unable to find wget or curl!")
    }
    val process = pb.inheritIO().start()
    val exitValue = process.waitFor()
    if (exitValue != 0) {
      throw new RuntimeException(s"Process $pb returned $exitValue")
    }
  }

  private def run(jar: Path, args: List[String]): Unit = {
    val command = List(
      java.toAbsolutePath.toString,
      "-jar",
      jar.toAbsolutePath.toString
    ) ::: args
    val pb = new ProcessBuilder(command: _*)
    val process = pb.inheritIO().start()
    val exitValue = process.waitFor()
    sys.exit(exitValue)
  }
}