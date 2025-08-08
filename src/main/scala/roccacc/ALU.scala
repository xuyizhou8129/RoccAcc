package roccacc

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.tile.CoreModule

//Now the logic assumes that the caclulation is done instantaneously once the data is fed
//Will need to change this after the ALU unit becomes more complicated

/** Externally-visible properties of the ALU.
  */
object ALU {
  /** The size of the ALU's internal functional unit's addresses */
  val SZ_ALU_FN = 4.W

  /** Unknown ALU function */
  def FN_X = BitPat("b????")
  def FN_ADD = BitPat("b0000")
  
  //Check if the command is an add
  def isAdd(cmd: UInt) = cmd === FN_ADD
}

/** Implementation of an ALU.
  * @param p Implicit parameter passed by the build system.
  */
class ALU(implicit p: Parameters) extends CoreModule()(p) {
  import ALU._ // Import ALU object, so we do not have to fully-qualify names
  val io = IO(new Bundle {
    val dw = Input(UInt(1.W))  // Data width: 0=32-bit, 1=64-bit
    val fn = Input(Bits(SZ_ALU_FN))
    // The two register content values passed over the RoCCCommand are xLen wide
    val in1 = Input(UInt(xLen.W))
    val in2 = Input(UInt(xLen.W))
    val out = Output(UInt(xLen.W))
    val valid = Output(Bool())
  })

  val alu_output_valid = RegInit(false.B)
  
  // Check if addition is required
  val is_addition = isAdd(io.fn)
  val is_addition_prev = RegInit(false.B)
  // Detect rising edge of is_addition
  val is_addition_rising = is_addition && !is_addition_prev
  val is_addition_falling = !is_addition && is_addition_prev
  
  // Default outputs
  io.out := 0.U(xLen.W)
  io.valid := alu_output_valid
  
  when(is_addition) {
    io.out := io.in1 + io.in2
  }
  
  // Set valid only on rising edge
  when(is_addition_rising) {
    alu_output_valid := true.B
    is_addition_prev := true.B
  }
  when(alu_output_valid) {
    alu_output_valid := false.B
  }
  when(is_addition_falling) {
    is_addition_prev := false.B
  }
}