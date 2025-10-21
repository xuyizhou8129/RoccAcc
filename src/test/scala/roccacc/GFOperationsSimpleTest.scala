package roccacc

import chisel3._
import chisel3.util._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.ParallelTestExecution
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.tile.TileKey
import freechips.rocketchip.tile.TileParams
import org.chipsalliance.cde.config.Config
import freechips.rocketchip.rocket.RocketCoreParams

class GFOperationsSimpleTest extends AnyFunSpec with ParallelTestExecution {

  val fieldSize = 8

  // Create Parameters with minimal tile configuration for testing
  implicit val p: Parameters = new Config((site, here, up) => {
    case TileKey => new TileParams {
      val core = RocketCoreParams(nPMPs = 0)
      val icache = None
      val dcache = None
      val btb = None
      val tileId = 0
      val blockerCtrlAddr = None
      val baseName = "test_tile"
      val clockSinkParams = freechips.rocketchip.prci.ClockSinkParameters()
      val uniqueName = "test_tile_0"
    }
  })

  describe("GFOperations Module") {

    it("initially has invalid output") {
      simulate(new GFOperations(fieldSize)) { dut =>
        dut.io.valid.expect(false.B)
      }
    }

    it("performs GF addition (XOR)") {
      simulate(new GFOperations(fieldSize)) { dut =>
        // Test 0 + 1 = 1
        dut.io.fn.poke(0.U) // FN_ADD = BitPat("b0000")
        dut.io.operand1.poke(0.U)
        dut.io.operand2.poke(1.U)
        dut.clock.step(1)
        dut.io.result.expect(1.U)
        dut.io.valid.expect(true.B)
        
        // Test 1 + 1 = 0 (XOR)
        dut.io.fn.poke(0.U) // FN_ADD = BitPat("b0000")
        dut.io.operand1.poke(1.U)
        dut.io.operand2.poke(1.U)
        dut.clock.step(1)
        dut.io.result.expect(0.U)
        dut.io.valid.expect(true.B)
        
        // Test 5 + 3 = 6 (XOR)
        dut.io.fn.poke(0.U) // FN_ADD = BitPat("b0000")
        dut.io.operand1.poke(5.U)
        dut.io.operand2.poke(3.U)
        dut.clock.step(1)
        dut.io.result.expect(6.U) // 5 XOR 3 = 6
        dut.io.valid.expect(true.B)
      }
    }
  }
}
