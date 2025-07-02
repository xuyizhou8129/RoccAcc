organization := "edu.northwestern.eecs"

version := "0.0"

name := "vcode-rocc"
// JAR ends up as edu.northwestern.eecs:vcode-rocc:0.0

// Chosen to be the same version as rocket-chip
scalaVersion := "2.12.15"

val chiselVersion = "3.5.2"
lazy val chiselSettings = Seq(
  // pull in the Chisel3 runtime library
  libraryDependencies ++= Seq("edu.berkeley.cs" %% "chisel3" % chiselVersion),
  // register the Chisel macro plugin so that Chisel’s hardware-generation macros expand correctly
  addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full)
)