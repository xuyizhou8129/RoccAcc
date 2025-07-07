package roccacc

import chisel3.util._

package object constants extends
    roccacc.OptionConstants
{}

/** Mixin for constants representing options.
 */
trait OptionConstants {
  def X = BitPat("b?") //Don't care, One bit wide
  def Y = BitPat("b1") //Yes I have a Y-operand, One bit wide
  def N = BitPat("b0") //I don't have a operand here, One bit wide
}