#!/usr/bin/env bash
set -euo pipefail

# 1) Build the Verilator testbench in rocc-acc/test
echo "=== Building RoCC testbench ==="
pushd generators/rocc-acc/test >/dev/null
make clean
make build
popd >/dev/null

# 2) Compile the Chisel/Scala RoCC generator
echo "=== Compiling Chisel/Scala RoCC generator ==="
pushd . >/dev/null
cd /pool/xuyi/Project1_C/chipyardfork/accelerator
sbt "project roccacc" compile
popd >/dev/null

# 3) Source env and build the Verilator simulator
echo "=== Building Verilator simulator ==="
pushd /pool/xuyi/Project1_C/chipyardfork/accelerator >/dev/null
source env.sh
cd sims/verilator
make CONFIG=RoccAccConfig
popd >/dev/null

# 4) Run the simulation
echo "=== Running simulation ==="
pushd /pool/xuyi/Project1_C/chipyardfork/accelerator/sims/verilator >/dev/null
./simulator-chipyard.harness-RoccAccConfig \
  /pool/xuyi/Project1_C/chipyardfork/accelerator/generators/rocc-acc/test/bin/rocc_add.riscv
popd >/dev/null

echo "=== All done! ==="
