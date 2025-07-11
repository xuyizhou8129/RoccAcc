organization := "edu.northwestern.eecs"

version := "0.0"

name := "rocc-acc"

// Chosen to be the same version as rocket-chip
scalaVersion := "2.13.10"
// ThisBuild / scalaVersion := "2.13.10"

// Chosen to be the same as Chipyard
val chiselVersion = "6.7.0"

lazy val chiselSettings = Seq(
  libraryDependencies ++= Seq("org.chipsalliance" %% "chisel" % chiselVersion),
  addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full)
)