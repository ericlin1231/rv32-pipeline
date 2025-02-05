package core

import chisel3._

import parameters.System

class Control extends Module {
  val io = IO(new Bundle {
    val IF_jump_flag = Input(Bool())
    val ID_jump_inst = Input(Bool())
    val ID_rs1       = Input(UInt(System.PhysicalRegisterAddrWidth))
    val ID_rs2       = Input(UInt(System.PhysicalRegisterAddrWidth))
    val EX_MEM_rEn   = Input(Bool())
    val EX_rd        = Input(UInt(System.PhysicalRegisterAddrWidth))
    val MEM_rEn      = Input(Bool())
    val MEM_rd       = Input(UInt(System.PhysicalRegisterAddrWidth))

    val IF_flush = Output(Bool())
    val ID_flush = Output(Bool())
    val PC_stall = Output(Bool())
    val IF_stall = Output(Bool())
  })

  io.IF_flush := false.B
  io.ID_flush := false.B
  io.PC_stall := false.B
  io.IF_stall := false.B
  when(
    ((io.ID_jump_inst || io.EX_MEM_rEn)
      && io.EX_rd =/= 0.U
      && (io.EX_rd === io.ID_rs1 || io.EX_rd === io.ID_rs2))
    || (io.ID_jump_inst
        && io.MEM_rEn
        && io.MEM_rd =/= 0.U 
        && (io.MEM_rd === io.ID_rs1 || io.MEM_rd === io.ID_rs2))
  ) {
    io.ID_flush := true.B
    io.PC_stall := true.B
    io.IF_stall := true.B
  }.elsewhen(io.IF_jump_flag) {
    io.IF_flush := true.B
  }
}

