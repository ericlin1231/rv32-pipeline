package core

import chisel3._

import bundle.PipelineBufferRegisterBundle
import parameters.System

class PipelineRegister(width: Int = System.DataBits, defaultValue: UInt = 0.U) extends Module {
  val io = IO(new Bundle {
    val Port  = new PipelineBufferRegisterBundle

    val in    = Input(UInt(width.W))
    val out   = Output(UInt(width.W))
  })

  val reg = RegInit(UInt(width.W), defaultValue)
  val out = RegInit(UInt(width.W), defaultValue)

  when(io.Port.flush) {
    out := defaultValue
    reg := defaultValue
  } .elsewhen(io.Port.stall) {
    out := reg
  } .otherwise {
    reg := io.in
    out := io.in
  }

  io.out := out
}

