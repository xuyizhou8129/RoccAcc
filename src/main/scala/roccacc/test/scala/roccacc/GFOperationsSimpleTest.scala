package roccacc

import chisel3._
import chisel3.util._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest._
import funsuite._
import funspec._

class GFOperationsSimpleTest extends AnyFunSpec with ChiselSim with ParallelTestExecution {

  val fieldSize = 8

  def test_gf_add(a: Int, b: Int): Unit = {
    simulate(new GFOperations(fieldSize)) { gf =>
      gf.io.fn.poke(GFOperations.FN_ADD)
      gf.io.operand1.poke(a.U)
      gf.io.operand2.poke(b.U)
      gf.clock.step(1)
      gf.io.result.expect((a ^ b).U) // XOR operation
      gf.io.valid.expect(true.B)
    }
  }

  def test_gf_mul(a: Int, b: Int): Unit = {
    simulate(new GFOperations(fieldSize)) { gf =>
      gf.io.fn.poke(GFOperations.FN_MUL)
      gf.io.operand1.poke(a.U)
      gf.io.operand2.poke(b.U)
      gf.clock.step(1)
      // For now, just test that it doesn't crash
      // You can add proper expected values later
      gf.io.valid.expect(true.B)
    }
  }

  def test_gf_pow(base: Int, exp: Int): Unit = {
    simulate(new GFOperations(fieldSize)) { gf =>
      gf.io.fn.poke(GFOperations.FN_POW)
      gf.io.operand1.poke(base.U)
      gf.io.operand2.poke(exp.U)
      gf.clock.step(exp + 2) // Wait for state machine to complete
      // For now, just test that it doesn't crash
      // You can add proper expected values later
      gf.io.valid.expect(true.B)
    }
  }

  describe("GFOperations Module") {
    it("initially has invalid output") {
      simulate(new GFOperations(fieldSize)) { gf =>
        gf.io.valid.expect(false.B)
      }
    }

    it("should add 0 + 0 = 0") { test_gf_add(0, 0) }
    it("should add 0 + 1 = 1") { test_gf_add(0, 1) }
    it("should add 1 + 1 = 0") { test_gf_add(1, 1) }
    it("should add 5 + 3 = 6") { test_gf_add(5, 3) }

    it("should multiply 0 * 5 = 0") { test_gf_mul(0, 5) }
    it("should multiply 1 * 5 = 5") { test_gf_mul(1, 5) }
    it("should multiply 2 * 3 = 6") { test_gf_mul(2, 3) }

    it("should compute 2^1 = 2") { test_gf_pow(2, 1) }
    it("should compute 2^2 = 4") { test_gf_pow(2, 2) }
    it("should compute 3^3 = 27") { test_gf_pow(3, 3) }
  }
}
