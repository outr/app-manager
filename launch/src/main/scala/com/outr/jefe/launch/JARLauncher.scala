package com.outr.jefe.launch

import java.io.{File, FileInputStream}
import java.util.jar.JarInputStream

import com.outr.jefe.launch.jmx.JMXConfig
import scala.collection.mutable.ListBuffer

class JARLauncher(name: String,
                  val jars: List[File],
                  val mainClass: Option[String] = None,
                  val jvmArgs: List[String] = Nil,        // TODO: support typed entries to replace String
                  val args: List[String] = Nil,           // TODO: support typed entries to replace String
                  val jmxConfig: Option[JMXConfig] = Some(JMXConfig()),
                  workingDirectory: File = new File("."),
                  environment: Map[String, String] = Map.empty,
                  loggerId: Long = scribe.Logger.rootId,
                  background: Boolean = false)
  extends ProcessLauncher(
    name = name,
    commands = JARLauncher.buildCommands(jars, mainClass, jvmArgs, args, jmxConfig),
    workingDirectory = workingDirectory,
    environment = environment,
    loggerId = loggerId,
    background = background
  )

object JARLauncher {
  private lazy val javaHome = System.getProperty("java.home")
  private lazy val fileSeparator = System.getProperty("file.separator")
  private lazy val pathSeparator = System.getProperty("path.separator")

  lazy val Java: String = {
    val extension = if (fileSeparator == "/") "" else "w.exe"
    s"$javaHome${fileSeparator}bin${fileSeparator}java$extension"
  }

  private def buildCommands(jars: List[File],
                            mainClassOption: Option[String],
                            jvmArgs: List[String],
                            args: List[String],
                            jmxConfig: Option[JMXConfig]): List[String] = {
    val mainClass = mainClassOption.orElse {
      val jar = jars.head
      val input = new JarInputStream(new FileInputStream(jar))
      try {
        val manifest = input.getManifest
        Option(manifest.getMainAttributes.getValue("Main-Class"))
      } finally {
        input.close()
      }
    }.getOrElse(throw new RuntimeException(s"No Main-Class defined in launcher or in manifest for ${jars.head.getName}"))
    val b = ListBuffer.empty[String]
    b += Java
    jmxConfig.foreach(c => b ++= c.args)
    b ++= jvmArgs
    b += "-cp"
    b += jars.map(_.getAbsolutePath).mkString(pathSeparator)
    b += mainClass
    b ++= args
    b.toList
  }
}