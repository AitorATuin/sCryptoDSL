import sbt._
import sbt.Keys._

object sCrytoDSL extends Build {

  lazy val validations = Project(
    id = "sCryptoDSL",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "sCryptoDSL",
      organization := "com.logikujo",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.0"
      // add other settings here
    )
  )
}
