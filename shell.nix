{ pkgs ? import <nixpkgs> {} }:

with pkgs.lib;

pkgs.mkShell {
  name = "vcode-rocc-shell";

  nativeBuildInputs = with pkgs; [
    sbt      # Scala Build Tool
    scala    # Compiler
    scalafmt # Linter
    metals   # LSP Server

    # keep this line if you use bash
    bashInteractive
  ];
}