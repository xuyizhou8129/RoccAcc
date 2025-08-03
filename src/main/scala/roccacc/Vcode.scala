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
  val alu = Module(new ALU)
  val alu_out = Wire(UInt())
  // Hook up the ALU to RoccAcc signals
  alu.io.dw := 1.U(1.W)  // Use 64-bit operations for now
  alu.io.fn := ctrl_sigs.alu_fn
  // Use fetched data, otherwise the inputs are 0
  alu.io.in1 := Mux(data_fetcher.io.data1_valid, data_fetcher.io.data1, 0.U)
  alu.io.in2 := Mux(data_fetcher.io.data2_valid, data_fetcher.io.data2, 0.U)
  alu_out := alu.io.out
  // alu_cout := alu.io.cout

  /***************
   * RESPOND
   **************/
  // Remember when we need to respond (since cmd.valid is only high for one cycle)
  val response_needed = RegInit(false.B)
  val response_fed = RegInit(false.B)
  val response_finished = RegInit(false.B)
  
  // Set response_needed when we get a valid command that needs a response
  when(cmd.valid && ctrl_sigs.legal && rocc_inst.xd) {
    response_needed := true.B
  }
  
  // Clear response_needed when we send the response, ensures that we only send one response
  when(io.resp.ready && response_needed && data_fetcher.io.done) {
    response_needed := false.B
  }
  
  // Check if the accelerator needs to respond
  val response_required = response_needed && data_fetcher.io.done
  
  // Debug print when response_needed is set
  when(cmd.valid && ctrl_sigs.legal && rocc_inst.xd) {
    if(p(RoccAccPrintfEnable)) {
      printf("DEBUG: response_needed set to TRUE\n")
      printf("DEBUG: data_fetcher.io.busy=%d, data_fetcher.io.done=%d\n", 
        data_fetcher.io.busy, data_fetcher.io.done)
    }
  }

  val response = Reg(new RoCCResponse)
  
  // Update response data when data fetching is complete and response is required
  when(response_required) {
    response.data := alu_out
    response.rd := rocc_inst.rd
    response_fed := true.B
  }
  // Send response to main processor
  /* TODO: Response can only be sent once all memory transactions and arithmetic
   * operations have completed. */
  when(response_fed && io.resp.ready) {
    if(p(RoccAccPrintfEnable)) {
      printf("DEBUG: Calling io.resp.enq(response)\n")
      printf("Main processor ready for response? %d\n", io.resp.ready)
      printf("Sending response: rd=%x data=%x (data1=%x + data2=%x)\n", 
        response.rd, response.data, data_fetcher.io.data1, data_fetcher.io.data2)
    }
    io.resp.enq(response) // Sends response & sets valid bit
    response_fed := false.B
    response_finished := true.B
    if(p(RoccAccPrintfEnable)) {
      printf("RoccAcc accelerator made response bits valid? %d\n", io.resp.valid)
    }
    // TODO: Find way to make valid response false when no response needed or ready
    // io.resp.valid := false.B // Always invalid response until otherwise
  }
  // Detect when response_finished becomes true (edge detection)
  val response_finished_prev = RegNext(response_finished, false.B)
  when(response_finished && !response_finished_prev) {
    io.resp.valid := false.B
    response_finished := false.B
  }
  
  // Debug print to track io.resp.valid changes
  val resp_valid_prev = RegNext(io.resp.valid, false.B)
  when(io.resp.valid && !resp_valid_prev) {
    if(p(RoccAccPrintfEnable)) {
      printf("DEBUG: io.resp.valid became TRUE\n")
    }
  }
  when(!io.resp.valid && resp_valid_prev) {
    if(p(RoccAccPrintfEnable)) {
      printf("DEBUG: io.resp.valid became FALSE\n")
    }
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