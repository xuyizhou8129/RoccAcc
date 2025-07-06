# VCode ROCC Accelerator - Chipyard Integration Guide

## Step 1: Register as Chipyard Submodule

In your Chipyard repository, add this repository as a submodule:

```bash
# Navigate to Chipyard root
cd chipyard

# Add the submodule (replace with your actual repository URL)
git submodule add <your-repo-url> generators/vcode-rocc

# Force it to be at a specific commit (optional)
cd generators/vcode-rocc
git checkout <specific-commit-hash>
cd ../..

# Commit the submodule addition
git add .gitmodules generators/vcode-rocc
git commit -m "Add vcode-rocc accelerator as submodule"
```

## Step 2: Update Chipyard's build.sbt

Edit `build.sbt` in the Chipyard root to include your project:

```scala
// Add this to the existing build.sbt
lazy val vcodeRocc = (project in file("generators/vcode-rocc"))
  .dependsOn(rocketchip)
  .settings(commonSettings)

// Add vcodeRocc to the aggregate list
lazy val chipyard = (project in file("generators/chipyard"))
  .dependsOn(rocketchip, boom, hwacha, sifive_blocks, vcodeRocc) // Add vcodeRocc here
  .settings(commonSettings)
```

## Step 3: Create Chipyard Configuration

Create a new configuration file in `generators/chipyard/src/main/scala/config/`:

```scala
// File: generators/chipyard/src/main/scala/config/VCodeConfigs.scala
package chipyard.config

import org.chipsalliance.cde.config._
import freechips.rocketchip.subsystem._
import vcoderocc._

class VCodeConfig extends Config(
  new WithVCodeAccel ++
  new WithVCodePrintf ++  // Optional: for debug prints
  new WithNBigCores(1) ++
  new BaseConfig
)

class VCodeSmallConfig extends Config(
  new WithVCodeAccel ++
  new WithVCodePrintf ++
  new WithNBigCores(1) ++
  new WithCoherentBusTopology ++
  new BaseConfig
)
```

## Step 4: Create Pingpong Test

Create a pingpong test in your test directory:

```c
// File: test/src/pingpong.c
#include <rocc.h>
#include <stdio.h>

int main() {
    int ping = 1;
    int pong = 0;
    int result;
    
    printf("Starting pingpong test...\n");
    
    // Send ping value to accelerator
    ROCC_INSTRUCTION_DSS(0, result, &ping, &pong, 0);
    
    printf("Ping: %d, Pong: %d, Result: %d\n", ping, pong, result);
    
    // Send pong value back
    ping = 0;
    pong = 1;
    ROCC_INSTRUCTION_DSS(0, result, &ping, &pong, 0);
    
    printf("Ping: %d, Pong: %d, Result: %d\n", ping, pong, result);
    
    return 0;
}
```

Update `test/src/modules.mk`:
```makefile
TEST_SRCS += rocc_add.c host_add.c \
             rocc_illegal.c rocc_illegal_nonblocking.c \
             pingpong.c
```

## Step 5: Build and Run with Verilator

```bash
# Navigate to Chipyard root
cd chipyard

# Build the simulator with your config
make -j$(nproc) CONFIG=VCodeConfig

# Run the pingpong test
./simulator-chipyard-VCodeConfig \
  -v test/vcode-rocc/test/bin/pingpong.riscv \
  +verbose
```

## Alternative: Run Existing Tests

You can also run your existing tests:

```bash
# Build your tests
cd test
make build

# Run with Chipyard simulator
./simulator-chipyard-VCodeConfig \
  -v bin/rocc_add.riscv \
  +verbose
```

## Troubleshooting

1. **Module not found**: Ensure your submodule is properly added and at the correct commit
2. **Compilation errors**: Check that your Scala version matches Chipyard's requirements
3. **Simulator not found**: Make sure you built the simulator with the correct config
4. **Test binary not found**: Ensure your test is compiled for RISC-V and the path is correct

## Notes

- Your VCode accelerator uses `OpcodeSet.custom0` (funct7 = 0x0B)
- The accelerator implements basic arithmetic operations through the ALU
- Debug prints can be enabled with `WithVCodePrintf` config
- Make sure your RISCV environment variable is set correctly 