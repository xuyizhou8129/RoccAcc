organization := "edu.northwestern.eecs"

version := "0.0"

name := "rocc-acc"

// Chosen to be the same version as rocket-chip
scalaVersion := "2.13.10"
// ThisBuild / scalaVersion := "2.13.10"

// Chosen to be the same as Chipyard
val chiselVersion = "3.6.1"

lazy val chiselSettings = Seq(
  libraryDependencies ++= Seq(
    "edu.berkeley.cs" %% "chisel3" % chiselVersion,
    "edu.berkeley.cs" %% "rocketchip" % "1.6.0"
  ),
  addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full)
)