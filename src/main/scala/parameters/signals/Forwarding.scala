package parameters

import chisel3._

object ForwardingType {
  val NoForward      = 0.U(2.W)
  val ForwardFromMEM = 1.U(2.W)
  val ForwardFromWB  = 2.U(2.W)
}
