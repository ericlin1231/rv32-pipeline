package core

import chisel3._

import parameters.System

class PipelineRegister(width: Int = System.DataBits, defaultValue: UInt = 0.U) extends Module {
  val io = IO(new Bundle {
    val stall = Input(Bool())
    val flush = Input(Bool())

    val in    = Input(UInt(width.W))
    val out   = Output(UInt(width.W))
  })

  val reg = RegInit(UInt(width.W), defaultValue)
  val out = RegInit(UInt(width.W), defaultValue)

  when(io.flush) {
    out := defaultValue
    reg := defaultValue
  } .elsewhen(io.stall) {
    out := reg
  } .otherwise {
    reg := io.in
    out := io.in
  }

  io.out := out
}

