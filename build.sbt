organization := "edu.berkeley.cs"

version := "1.0"

name := "rocc-acc"
// JAR ends up as edu.berkeley.cs:rocc-acc:1.0

// Chosen to be the same version as rocket-chip
scalaVersion := "2.12.15"

val chiselVersion = "3.5.2"
lazy val chiselSettings = Seq(
  // pull in the Chisel runtime library
  libraryDependencies ++= Seq("edu.berkeley.cs" %% "chisel3" % chiselVersion),
  addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full)
)

// Apply the Chisel settings
lazy val roccacc = (project in file("."))
  .settings(chiselSettings)