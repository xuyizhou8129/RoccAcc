package roccacc

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters
import org.chipsalliance.cde.tile.CoreModule

/** Externally-visible properties of the ALU.
  */
object ALU {
  /** The size of the ALU's internal functional unit's addresses */
  val SZ_ALU_FN = 4.W

  /** Unknown ALU function */
  def FN_X = BitPat("b????")
  def FN_ADD = BitPat("b0000")
}

/** Implementation of an ALU.
  * @param p Implicit parameter passed by the build system.
  */
class ALU(implicit p: Parameters) extends CoreModule()(p) {
  import ALU._ // Import ALU object, so we do not have to fully-qualify names
  val io = IO(new Bundle {
    val fn = Input(Bits(SZ_ALU_FN))
    // The two register content values passed over the RoCCCommand are xLen wide
    val in1 = Input(UInt(xLen.W))
    val in2 = Input(UInt(xLen.W))
    val out = Output(UInt(xLen.W))
    val cout = Output(UInt(xLen.W))
  })

  // ADD/SUB
  io.out := io.in1 + io.in2
}