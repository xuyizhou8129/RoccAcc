#!/bin/bash

# Test script for VCode ROCC accelerator pingpong test

set -e

echo "=== VCode ROCC Accelerator Pingpong Test ==="

# Check if RISCV is set
if [ -z "$RISCV" ]; then
    echo "Error: RISCV environment variable is not set"
    echo "Please set RISCV to point to your RISC-V toolchain"
    exit 1
fi

echo "Using RISCV toolchain at: $RISCV"

# Build the tests
echo "Building tests..."
cd test
make clean
make build

# Check if pingpong binary was created
if [ ! -f "bin/pingpong.riscv" ]; then
    echo "Error: pingpong.riscv binary was not created"
    exit 1
fi

echo "Test binary created successfully: bin/pingpong.riscv"

# Display binary info
echo "Binary information:"
file bin/pingpong.riscv
echo ""

echo "=== Test Ready ==="
echo "To run with Chipyard simulator:"
echo "1. Follow the integration steps in chipyard-integration-steps.md"
echo "2. Build Chipyard with: make CONFIG=VCodeConfig"
echo "3. Run with: ./simulator-chipyard-VCodeConfig -v test/vcode-rocc/test/bin/pingpong.riscv"
echo ""
echo "For local testing (if you have a RISC-V simulator):"
echo "qemu-riscv64-static bin/pingpong.riscv" 