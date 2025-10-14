package roccacc

import chisel3._
import chisel3.util._
import chisel3.simulator._
import svsim._

import org.scalatest._
import funsuite._
import funspec._

class GFOperationsSimpleTest extends AnyFunSpec with ParallelTestExecution {

  describe("GFOperations") {

    it("adds 0 + 1 = 1") {
      val simulator = new VerilatorSimulator("test_run_dir/roccacc/GFOperationsAdd")
      val result = simulator.simulate(new SimpleGFOperations(fieldSize = 4)) { module =>
        val dut = module.wrapped
        val fn = module.port(dut.io.fn)
        val operand1 = module.port(dut.io.operand1)
        val operand2 = module.port(dut.io.operand2)
        val result = module.port(dut.io.result)
        val clock = module.port(dut.clock)
        
        operand1.set(0)
        operand2.set(1)
        fn.set(0)   // e.g., 0 = add
        clock.tick(1, 1, 0, 1)  // timestepsPerPhase, cycles, inPhaseValue, outOfPhaseValue
        assert(result.get().asBigInt == 1)
      }
    }

    it("multiplies 2 * 3") {
      val simulator = new VerilatorSimulator("test_run_dir/roccacc/GFOperationsMul")
      val result = simulator.simulate(new SimpleGFOperations(4)) { module =>
        val dut = module.wrapped
        val fn = module.port(dut.io.fn)
        val operand1 = module.port(dut.io.operand1)
        val operand2 = module.port(dut.io.operand2)
        val result = module.port(dut.io.result)
        val clock = module.port(dut.clock)
        
        operand1.set(2)
        operand2.set(3)
        fn.set(1)   // e.g., 1 = mul
        clock.tick(1, 1, 0, 1)  // timestepsPerPhase, cycles, inPhaseValue, outOfPhaseValue
        // replace 6 with the expected GF result for your field/poly
        assert(result.get().asBigInt == 6)
      }
    }
  }
}

// Simple version of GFOperations that doesn't extend CoreModule
class SimpleGFOperations(fieldSize: Int = 4) extends Module {
  val io = IO(new Bundle {
    val fn = Input(UInt(4.W))           // Operation function code
    val operand1 = Input(UInt(fieldSize.W))  // First operand
    val operand2 = Input(UInt(fieldSize.W))  // Second operand
    val result = Output(UInt(fieldSize.W))  // Result
  })

  // Simple GF(2^4) operations
  // For now, just implement basic addition (XOR) and multiplication
  io.result := 0.U
  
  when(io.fn === 0.U) {
    // GF Addition (XOR)
    io.result := io.operand1 ^ io.operand2
  }.elsewhen(io.fn === 1.U) {
    // GF Multiplication (simplified - just regular multiplication for now)
    io.result := io.operand1 * io.operand2
  }
}

class VerilatorSimulator(val workspacePath: String) extends SingleBackendSimulator[verilator.Backend] {
  val backend = verilator.Backend.initializeFromProcessEnvironment()
  val tag = "verilator"
  val commonCompilationSettings = CommonCompilationSettings()
  val backendSpecificCompilationSettings = verilator.Backend.CompilationSettings()
}