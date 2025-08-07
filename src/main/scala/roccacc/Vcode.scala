package roccacc

import chisel3._
import chisel3.util._
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.rocket.constants.MemoryOpConstants

/** The outer wrapping class for the RoccAcc accelerator.
  *
  * @constructor Create a new RoccAcc accelerator interface using one of the
  * custom opcode sets.
  * @param opcodes The custom opcode set to use.
  * @param p The implicit key-value store of design parameters for this design.
  * This value is passed by the build system. You do not need to worry about it.
  */
class RoccAcc(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(opcodes) {
  override lazy val module = new RoccAccImp(this)
}

/** Implementation class for the RoccAcc accelerator.
  *
  * @constructor Create a new RoccAcc accelerator implementation, attached to
  * one RoccAcc interface with one of the custom opcode sets.
  * @param outer The "interface" for the accelerator to attach to.
  * This separation allows us to attach multiple of these accelerators to
  * different HARTs, and multiple to attach to a single HART using different
  * custom opcode sets.
  */
class RoccAccImp(outer: RoccAcc) extends LazyRoCCModuleImp(outer) {
  // io is "implicit" because we inherit from LazyRoCCModuleImp.
  // io is the RoCCCoreIO
  val cmd = Queue(io.cmd)
  val rocc_cmd = cmd.bits // The entire RoCC Command provided to the accelerator
  val rocc_inst = rocc_cmd.inst // The customX instruction in instruction stream
  cmd.ready := true.B // Always ready to accept a command

  /* Create the decode table at the top-level of the implementation
   * If additional instructions are added as separate classes in Instructions.scala
   * they can be added above BinOpDecode class. */
  val decode_table = {
    Seq(new BinOpDecode)
  } flatMap(_.decode_table)

  /***************
   * DECODE
   **************/
  // Decode instruction, yielding control signals
  val ctrl_sigs = Wire(new CtrlSigs()).decode(rocc_inst.funct, decode_table)

  // If invalid instruction, raise exception
  val exception = cmd.valid && !ctrl_sigs.legal
  io.interrupt := exception
  when(exception) {
    if(p(RoccAccPrintfEnable)) {
      printf("Raising exception to processor through interrupt!\nILLEGAL INSTRUCTION!\n");
    }
  }

  /* The valid bit is raised to true by the main processor when the command is
   * sent to the DecoupledIO Queue. */
  when(cmd.valid) {
    // TODO: Find a nice way to condense these conditional prints
    if(p(RoccAccPrintfEnable)) {
      printf("Got funct7 = 0x%x\trs1.val=0x%x\trs2.val=0x%x\n",
        rocc_inst.funct, rocc_cmd.rs1, rocc_cmd.rs2)
      printf("The instruction legal: %d\n", ctrl_sigs.legal)
    }
  }

  /***************
   * DATA FETCH
   * Most instructions pass pointers to vectors, so we need to fetch that before
   * operating on the data.
   **************/
  val data_fetcher = Module(new DCacheFetcher)
  
  // Connect memory interface using separated req/resp like working implementation
  data_fetcher.io.req <> io.mem.req
  data_fetcher.io.resp <> io.mem.resp
  
  // Control signals
  data_fetcher.io.start := cmd.valid && ctrl_sigs.legal
  data_fetcher.io.addr1 := rocc_cmd.rs1
  data_fetcher.io.addr2 := rocc_cmd.rs2
  
  // Debug print when data fetcher starts
  when(cmd.valid && ctrl_sigs.legal) {
    if(p(RoccAccPrintfEnable)) {
      printf("DEBUG: Starting data fetcher, busy=%d\n", data_fetcher.io.busy)
    }
  }

  /***************
   * EXECUTE
   **************/
  val alu = Module(new roccacc.ALU)
  val alu_out = Wire(UInt())
  // Hook up the ALU to RoccAcc signals
  alu.io.dw := 1.U(1.W)  // Use 64-bit operations for now
  alu.io.fn := Mux(data_fetcher.io.data1_valid && data_fetcher.io.data2_valid, ctrl_sigs.alu_fn, 1.U)
  // Use fetched data, otherwise the inputs are 0
  alu.io.in1 := Mux(data_fetcher.io.data1_valid, data_fetcher.io.data1, 0.U)
  alu.io.in2 := Mux(data_fetcher.io.data2_valid, data_fetcher.io.data2, 0.U)
  alu_out := alu.io.out

  /***************
   * RESPOND
   **************/
  // Remember when we need to respond (since cmd.valid is only high for one cycle)
  val response_needed = RegInit(false.B)
  
  // Set response_needed when we get a valid command that needs a response
  when(cmd.valid && ctrl_sigs.legal && rocc_inst.xd) {
    response_needed := true.B
  }
  
  val response = Reg(new RoCCResponse)
  val response_valid = RegInit(false.B)
  
  // Default values
  io.resp.bits := response
  io.resp.valid := response_valid
  
  // Prepare response data when computation is complete
  when(alu.io.valid && response_needed && data_fetcher.io.done) { 
    response.data := alu_out
    response.rd := rocc_inst.rd
    response_valid := true.B
    response_needed := false.B
  }
  
  // Clear response valid when handshake occurs
  when(response_valid && io.resp.ready) {
    response_valid := false.B
  }
  }


/** Mixin to build a chip that includes a RoccAcc accelerator. */
class WithRoccAcc extends Config((site, here, up) => {
  case BuildRoCC => List (
    (p: Parameters) => {
      val roccAcc = LazyModule(new RoccAcc(OpcodeSet.custom0)(p))
      roccAcc
    })
})

/** Design-level configuration option to toggle the synthesis of print statements
  * in the synthesized hardware design.
  */
case object RoccAccPrintfEnable extends Field[Boolean](false)

/** Mixin to enable print statements from the synthesized design.
  * This mixin should only be used AFTER the WithRoccAcc mixin.
  */
class WithRoccAccPrintf extends Config((site, here, up) => {
  case RoccAccPrintfEnable => true
})