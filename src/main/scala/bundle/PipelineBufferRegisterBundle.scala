package bundle

import chisel3._

import parameters.System

class PipelineBufferRegisterBundle extends Bundle {
  val stall = Input(Bool())
  val flush = Input(Bool())
}
