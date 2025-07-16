#!/usr/bin/env bash
set -euo pipefail
# 1) Source env variables
echo "=== Sourcing env variables ==="
cd /pool/xuyi/Project1_C/chipyardfork/accelerator

# Set default values to avoid unbound variable errors
export LD_LIBRARY_PATH="${LD_LIBRARY_PATH:-}"
export RISCV="${RISCV:-}"

source env.sh

# 2) Build the Verilator testbench in rocc-acc/test
echo "=== Building RoCC testbench ==="
cd /pool/xuyi/Project1_C/chipyardfork/accelerator/generators/rocc-acc/test
make clean
make build

# 3) Compile the Chisel/Scala RoCC generator
echo "=== Compiling Chisel/Scala RoCC generator ==="
cd /pool/xuyi/Project1_C/chipyardfork/accelerator
sbt "project roccacc" compile

# 4) Source env and build the Verilator simulator
echo "=== Building Verilator simulator ==="
cd /pool/xuyi/Project1_C/chipyardfork/accelerator/sims/verilator
make CONFIG=RoccAccConfig

# 5) Run the simulation
echo "=== Running simulation ==="
cd /pool/xuyi/Project1_C/chipyardfork/accelerator/sims/verilator
./simulator-chipyard.harness-RoccAccConfig \
  /pool/xuyi/Project1_C/chipyardfork/accelerator/generators/rocc-acc/test/bin/rocc_add.riscv

echo "=== All done! ==="
