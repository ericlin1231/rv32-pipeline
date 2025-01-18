package bundle

import chisel3._

import parameters.System

class RegisterDebugBundle extends Bundle {
  val debug_read_address = Input(UInt(System.PhysicalRegisterAddrWidth))
  val debug_read_data    = Output(UInt(System.DataWidth))
}
