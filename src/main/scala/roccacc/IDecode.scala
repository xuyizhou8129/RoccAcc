package roccacc

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.tile.HasCoreParameters
import Instructions._
import roccacc.constants._
import ALU._

/** Trait holding an abstract (non-instantiated) mapping between the instruction
  * bit pattern and its control signals.
  */
trait DecodeConstants extends HasCoreParameters { // TODO: Not sure if extends needed
  /** Array of pairs (table) mapping between instruction bit patterns and control
    * signals. */
  val decode_table: Array[(BitPat, List[BitPat])]
}

/** Control signals in the processor.
  * These are set during decoding.
  */
class CtrlSigs extends Bundle { // TODO: Rename to BinOpCtrlSigs?
  /* All control signals used in this coprocessor
   * See rocket-chip's rocket/IDecode.scala#IntCtrlSigs#default */
  val legal = Bool() // Example control signal.
  val alu_fn = Bits(SZ_ALU_FN)
  /** List of default control signal values
    * @return List of default control signal values. */
  def default_decode_ctrl_sigs: List[BitPat] =
    List(N, FN_X)

  /** Decodes an instruction to its control signals.
    * @param inst The instruction bit pattern to be decoded.
    * @param table Table of instruction bit patterns mapping to list of control
    * signal values.
    * @return Sequence of control signal values for the provided instruction.
    */
  def decode(inst: UInt, decode_table: Iterable[(BitPat, List[BitPat])]) = {
    val decoder = freechips.rocketchip.rocket.DecodeLogic(inst, default_decode_ctrl_sigs, decode_table)
    /* Make sequence ordered how signals are ordered.
     * See rocket-chip's rocket/IDecode.scala#IntCtrlSigs#decode#sigs */
    val ctrl_sigs = Seq(legal, alu_fn)
    /* Decoder is a minimized truth-table. We partially apply the map here,
     * which allows us to apply an instruction to get its control signals back.
     * We then zip that with the sequence of names for the control signals. */
    ctrl_sigs zip decoder map{case(s,d) => s := d}
    this
  }
}

/** Class holding a table that implements the DecodeConstants table that mapping
  * a binary operation's instruction bit pattern to control signals.
  * @param p Implicit parameter of key-value pairs that can globally alter the
  * parameters of the design during elaboration.
  */
class BinOpDecode(implicit val p: Parameters) extends DecodeConstants {
  val decode_table: Array[(BitPat, List[BitPat])] = Array(
    PLUS_INT-> List(Y, FN_ADD))
}