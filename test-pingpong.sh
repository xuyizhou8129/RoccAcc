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
export MAKEFLAGS=-j24
make clean
make build

# 3) Compile the Chisel/Scala RoCC generator
echo "=== Compiling Chisel/Scala RoCC generator ==="
cd /pool/xuyi/Project1_C/chipyardfork/accelerator
sbt "project roccacc" compile

# 4) Source env and build the Verilator simulator, run the pingpong test, at the same time generate the vcd file for
echo "=== Building Verilator simulator ==="
cd /pool/xuyi/Project1_C/chipyardfork/accelerator/sims/verilator
export MAKEFLAGS=-j24
make CONFIG=RoccAccConfig BINARY=/pool/xuyi/Project1_C/chipyardfork/accelerator/generators/rocc-acc/test/bin/rocc_add.riscv LOADMEM=1 run-binary-debug

: <<'COMMENT'
# 6) Run the simulation
echo "=== Running simulation ==="
cd /pool/xuyi/Project1_C/chipyardfork/accelerator/sims/verilator
./simulator-chipyard.harness-RoccAccConfig \
  /pool/xuyi/Project1_C/chipyardfork/accelerator/generators/rocc-acc/test/bin/pingpong.riscv
COMMENT

echo "=== All done! ==="

