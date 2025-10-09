package roccacc

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.tile.CoreModule

/** Galois Field Operations Module
  * 
  * This module provides basic Galois Field arithmetic operations including:
  * - Addition (XOR in GF)
  * - Multiplication 
  * - Division
  * - Exponentiation
  * - Inverse computation
  * 
  * Currently supports GF(2^m) fields where m is configurable
  */
object GFOperations {
  /** Default field size for GF(2^8) */
  val DEFAULT_FIELD_SIZE = 8
  
  /** Function codes for different GF operations */
  val SZ_GF_FN = 4.W
  
  def FN_X = BitPat("b????")
  def FN_ADD = BitPat("b0000")  // GF Addition (XOR)
  def FN_MUL = BitPat("b0001")  // GF Multiplication
  def FN_DIV = BitPat("b0010")  // GF Division
  def FN_POW = BitPat("b0011")  // GF Exponentiation
  def FN_INV = BitPat("b0100")  // GF Inverse
  
  // Helper functions to check operation type
  def isAdd(cmd: UInt) = cmd === FN_ADD
  def isMul(cmd: UInt) = cmd === FN_MUL
  def isDiv(cmd: UInt) = cmd === FN_DIV
  def isPow(cmd: UInt) = cmd === FN_POW
  def isInv(cmd: UInt) = cmd === FN_INV
}

/** Implementation of Galois Field Operations
  * 
  * @param fieldSize The size of the Galois Field (e.g., 8 for GF(2^8))
  * @param p Implicit parameter passed by the build system
  */
class GFOperations(fieldSize: Int = GFOperations.DEFAULT_FIELD_SIZE)(implicit p: Parameters) extends CoreModule()(p) {
  import GFOperations._
  
  val io = IO(new Bundle {
    val fn = Input(Bits(SZ_GF_FN))           // Operation function code
    val operand1 = Input(UInt(fieldSize.W))  // First operand
    val operand2 = Input(UInt(fieldSize.W))  // Second operand (not used for inverse)
    val result = Output(UInt(fieldSize.W))   // Operation result
    val valid = Output(Bool())               // Result valid signal
  })
  
  // Internal registers for state management
  val result_valid = RegInit(false.B)
  val operation_active = RegInit(false.B)
  
  // Default outputs
  io.result := 0.U(fieldSize.W)
  io.valid := result_valid
  
  // GF Addition (XOR operation)
  when(isAdd(io.fn)) {
    io.result := io.operand1 ^ io.operand2
    result_valid := true.B
  }
  
  // GF Multiplication using polynomial multiplication
  when(isMul(io.fn)) {
    io.result := gfMultiply(io.operand1, io.operand2, fieldSize)
    result_valid := true.B
  }
  
  // GF Division (multiply by inverse)
  when(isDiv(io.fn)) {
    val inverse = gfInverse(io.operand2, fieldSize)
    io.result := gfMultiply(io.operand1, inverse, fieldSize)
    result_valid := true.B
  }
  
  // GF Exponentiation
  when(isPow(io.fn)) {
    io.result := gfPower(io.operand1, io.operand2, fieldSize)
    result_valid := true.B
  }
  
  // GF Inverse
  when(isInv(io.fn)) {
    io.result := gfInverse(io.operand1, fieldSize)
    result_valid := true.B
  }
  
  // Reset valid signal after one cycle
  when(result_valid) {
    result_valid := false.B
  }
  
  /** Galois Field multiplication using polynomial representation
    * 
    * @param a First operand
    * @param b Second operand  
    * @param fieldSize Size of the field
    * @return Result of GF multiplication
    */
  def gfMultiply(a: UInt, b: UInt, fieldSize: Int): UInt = {
    val irreduciblePoly = getIrreduciblePolynomial(fieldSize)
    val result = Wire(UInt(fieldSize.W))
    
    // Handle zero case
    when(a === 0.U || b === 0.U) {
      result := 0.U
    }.otherwise {
      // Parallel polynomial multiplication with inline reduction
      result := 0.U
      for (i <- 0 until fieldSize) {
        when(b(i)) {
          val shiftedA = a << i.U
          val reducedA = Wire(UInt(fieldSize.W))
          
          // Inline reduction: check if shifted value exceeds field size
          when(shiftedA(fieldSize * 2 - 1, fieldSize) =/= 0.U) {
            // Reduce by XORing with irreducible polynomial shifted appropriately
            reducedA := gfReduce(shiftedA, irreduciblePoly, fieldSize)
          }.otherwise {
            reducedA := shiftedA(fieldSize - 1, 0)
          }
          
          result := result ^ reducedA
        }
      }
    }
    
    result
  }
  
  /** Galois Field inverse using Extended Euclidean Algorithm
    * 
    * @param a Element to find inverse of
    * @param fieldSize Size of the field
    * @return Inverse of a in GF(2^fieldSize)
    */
  def gfInverse(a: UInt, fieldSize: Int): UInt = {
    val irreduciblePoly = getIrreduciblePolynomial(fieldSize)
    
    // Extended Euclidean Algorithm implementation
    // This is a simplified version - in practice, you might want to use
    // lookup tables or more efficient algorithms for larger fields
    val result = Wire(UInt(fieldSize.W))
    
    // For now, return a simple implementation
    // In a real implementation, you'd use the Extended Euclidean Algorithm
    when(a === 0.U) {
      result := 0.U  // No inverse for zero
    }.otherwise {
      result := gfPower(a, (fieldSize - 2).U, fieldSize)
    }
    result
  }
  
  /** Galois Field exponentiation using state machine
    * Simple approach: multiply base by itself 'exponent' times
    * 
    * @param base Base element
    * @param exponent Exponent
    * @param fieldSize Size of the field
    * @return Result of base^exponent in GF(2^fieldSize)
    */
  def gfPower(base: UInt, exponent: UInt, fieldSize: Int): UInt = {
    val result = Wire(UInt(fieldSize.W))
    
    // State machine registers
    val state = RegInit(0.U(2.W))  // 0: IDLE, 1: COMPUTING, 2: DONE
    val temp_result = RegInit(1.U(fieldSize.W))
    val original_base = RegInit(0.U(fieldSize.W))
    val temp_exp = RegInit(0.U(fieldSize.W))
    
    // Default outputs
    result := temp_result
    
    // State machine
    switch(state) {
      is(0.U) { // IDLE state
        // Initialize for new computation
        temp_result := 1.U
        original_base := base
        temp_exp := exponent
        state := 1.U
      }
      
      is(1.U) { // COMPUTING state
        // Check if we're done (exponent reached 0)
        when(temp_exp === 0.U) {
          state := 2.U
        }.otherwise {
          // Multiply result by original base once
          temp_result := gfMultiply(temp_result, original_base, fieldSize)
          // Reduce exponent by 1
          temp_exp := temp_exp - 1.U
        }
      }
      
      is(2.U) { // DONE state
        // Handle special cases and output result
        when(exponent === 0.U) {
          result := 1.U
        }.elsewhen(base === 0.U) {
          result := 0.U
        }.otherwise {
          result := temp_result
        }
        
        // Stay in DONE state until new computation starts
        // (This will be controlled by the calling logic)
      }
    }
    
    result
  }
  
  /** Reduce polynomial modulo irreducible polynomial
    * 
    * @param poly Polynomial to reduce
    * @param irreduciblePoly Irreducible polynomial
    * @param fieldSize Size of the field
    * @return Reduced polynomial
    */
  def gfReduce(poly: UInt, irreduciblePoly: UInt, fieldSize: Int): UInt = {
    val result = Wire(UInt(fieldSize.W))
    var temp = poly
    
    // Reduce by shifting and XORing with irreducible polynomial
    for (i <- (fieldSize * 2 - 1) to fieldSize by -1) {
      when(temp(i)) {
        temp := temp ^ (irreduciblePoly << (i - fieldSize).U)
      }
    }
    
    result := temp(fieldSize - 1, 0)
    result
  }
  
  /** Get irreducible polynomial for given field size
    * 
    * @param fieldSize Size of the field
    * @return Irreducible polynomial
    */
  def getIrreduciblePolynomial(fieldSize: Int): UInt = {
    fieldSize match {
      case 8 => "b100011101".U  // x^8 + x^4 + x^3 + x^2 + 1
      case 16 => "b10000000000011011".U  // x^16 + x^5 + x^3 + x + 1
      case 32 => "b100000000000000000000000000101101".U  // x^32 + x^7 + x^3 + x^2 + 1
      case _ => "b100011101".U  // Default to GF(2^8) polynomial
    }
  }
}

