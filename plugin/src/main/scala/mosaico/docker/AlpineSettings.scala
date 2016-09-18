package mosaico.docker

import java.io.File

import mosaico.common.MiscUtils
import mosaico.config.MosaicoConfigPlugin
import sbt._, Keys._
import scala.language.postfixOps
import MosaicoDockerPlugin.autoImport

trait AlpineSettings extends MiscUtils {
  this: AutoPlugin =>

  trait AlpineKeys {
    lazy val alpineBuild = inputKey[Seq[File]]("alpineBuild")
  }

  import autoImport._
  import MosaicoConfigPlugin.autoImport._

  val alpineBuildTask = alpineBuild := {
    val args: Seq[String] = replaceAtWithMap(Def.spaceDelimited("<arg>").parsed, prp.value)

    if (args.length < 3) {
      println("usage: alpineBuild {ALPINEBUILDIMAGE} {APKBUILD} {APKFILE}")
      Seq()
    } else {

      val base = baseDirectory.value
      val buildImage = args(0)
      val target = base / "target"
      val abuild = base / "abuild"
      val in = args(1)
      val inFile = abuild / in
      val out = args(2)
      val outFile = target / out

      val cmd =
        s"""docker run
            | -v ${abuild}:/home/abuild
            | -v ${target}:/home/packager/packages
            | ${buildImage} ${in} ${out}
            |""".stripMargin.replace('\n', ' ')

      debug(cmd)
      if (inFile.exists) {
        if (!outFile.exists) {
          println(s"*** not found ${outFile.getAbsolutePath} - building")
          cmd !
        } else {
          println(s"*** found ${outFile} - not building")
        }
        Seq(outFile)
      } else {
        println(s"*** not found ${inFile}")
        Seq()
      }
    }
  }

  val alpineSettings = Seq(
    alpineBuildTask
  )

}
