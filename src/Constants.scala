package vcoderocc

import chisel3.util._

package object constants extends
    vcoderocc.OptionConstants
{}

/** Mixin for constants representing options.
 */
trait OptionConstants {
  def X = BitPat("b?")
  def Y = BitPat("b1")
  def N = BitPat("b0")
}