package core

import chisel3._

import parameters.System

class Control extends Module {
  val io = IO(new Bundle {
    val jump_flag      = Input(Bool())                                 /* id.io.if_jump_flag */
    val jump_instAddr  = Input(Bool())                                 /* id.io.ctrl_jump_instruction */
    val rs1_id         = Input(UInt(System.PhysicalRegisterAddrWidth)) /* id.io.reg1_rAddr */
    val rs2_id         = Input(UInt(System.PhysicalRegisterAddrWidth)) /* id.io.reg2_rAddr */
    val memory_rEn_ex  = Input(Bool())                                 /* ID2EX.io.output_memory_rEn */
    val rd_ex          = Input(UInt(System.PhysicalRegisterAddrWidth)) /* ID2EX.io.output_regs_wAddr */
    val memory_rEn_mem = Input(Bool())                                 /* EX2MEM.io.output_memory_rEn */
    val rd_mem         = Input(UInt(System.PhysicalRegisterAddrWidth)) /* EX2MEM.io.output_regs_wAddr */

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
    ((io.jump_instruction_id || io.memory_rEn_ex) && io.rd_ex =/= 0.U && (io.rd_ex === io.rs1_id || io.rd_ex === io.rs2_id)) ||
      (io.jump_instruction_id && io.memory_rEn_mem && io.rd_mem =/= 0.U && (io.rd_mem === io.rs1_id || io.rd_mem === io.rs2_id))
  ) {
    io.ID_flush := true.B
    io.PC_stall := true.B
    io.IF_stall := true.B
  }.elsewhen(io.jump_flag) {
    io.IF_flush := true.B
  }
}

