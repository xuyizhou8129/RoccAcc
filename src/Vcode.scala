package vcoderocc

import chisel3._
import chisel3.util._
import freechips.rocketchip.tile._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import freechips.rocketchip.tilelink._

/** The outer wrapping class for the VCODE accelerator.
  *
  * @constructor Create a new VCode accelerator interface using one of the
  * custom opcode sets.
  * @param opcodes The custom opcode set to use.
  * @param p The implicit key-value store of design parameters for this design.
  * This value is passed by the build system. You do not need to worry about it.
  */
class VCodeAccel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(opcodes) {
  override lazy val module = new VCodeAccelImp(this)
}

/** Implementation class for the VCODE accelerator.
  *
  * @constructor Create a new VCODE accelerator implementation, attached to
  * one VCodeAccel interface with one of the custom opcode sets.
  * @param outer The "interface" for the accelerator to attach to.
  * This separation allows us to attach multiple of these accelerators to
  * different HARTs, and multiple to attach to a single HART using different
  * custom opcode sets.
  */
class VCodeAccelImp(outer: VCodeAccel) extends LazyRoCCModuleImp(outer) {
  val cmd = Queue(io.cmd)
  cmd.ready := true.B

  /* Create the decode table at the top-level of the implementation
   * If additional instructions are added as separate classes in Instructions.scala
   * they can be added above BinOpDecode class. */
  val decode_table = {
    Seq(new BinOpDecode)
  } flatMap(_.decode_table)
  // Add the control signals
  val ctrl_sigs = Reg(new CtrlSigs)
}

/** Mixin to build a chip that includes a VCode accelerator.
  */
class WithVCodeAccel extends Config((site, here, up) => {
  case BuildRoCC => List (
    (p: Parameters) => {
      val vcodeAccel = LazyModule(new VCodeAccel(OpcodeSet.custom0)(p))
      vcodeAccel
    })
})