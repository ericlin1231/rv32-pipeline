package core

import chisel3._

import parameters.System
import parameters.signals.ForwardingType

class Forwarding extends Module {
  val io = IO(new Bundle() {
    val rs1_id      = Input(UInt(Parameters.PhysicalRegisterAddrWidth)) /* ID.io.reg1_rAddr */
    val rs2_id      = Input(UInt(Parameters.PhysicalRegisterAddrWidth)) /* ID.io.reg2_rAddr */
    val rs1_ex      = Input(UInt(Parameters.PhysicalRegisterAddrWidth)) /* ID2EX.io.output_reg1_rAddr  */
    val rs2_ex      = Input(UInt(Parameters.PhysicalRegisterAddrWidth)) /* ID2EX.io.output_reg2_rAddr  */
    val rd_mem      = Input(UInt(Parameters.PhysicalRegisterAddrWidth)) /* EX2MEM.io.output_regs_wAddr */
    val reg_wEn_mem = Input(Bool())                                     /* EX2MEM.io.output_regs_wEn   */
    val rd_wb       = Input(UInt(Parameters.PhysicalRegisterAddrWidth)) /* MEM2WB.io.output_regs_wAddr */
    val reg_wEn_wb  = Input(Bool())                                     /* MEM2WB.io.output_regs_wEn   */

    val reg1_forward_id = Output(UInt(2.W)) /* ID.io.reg1_forward */
    val reg2_forward_id = Output(UInt(2.W)) /* ID.io.reg2_forward */
    val reg1_forward_ex = Output(UInt(2.W)) /* EX.io.reg1_forward */
    val reg2_forward_ex = Output(UInt(2.W)) /* EX.io.reg2_forward */
  })

  /* io.reg1_forward_ex */
  when(io.reg_wEn_mem && io.rs1_ex === io.rd_mem && io.rd_mem =/= 0.U) {
    io.reg1_forward_ex := ForwardingType.ForwardFromMEM
  }.elsewhen(io.reg_wEn_wb && io.rs1_ex === io.rd_wb && io.rd_wb =/= 0.U) {
    io.reg1_forward_ex := ForwardingType.ForwardFromWB
  }.otherwise {
    io.reg1_forward_ex := ForwardingType.NoForward
  }
  /* io.reg2_forward_ex */
  when(io.reg_wEn_mem && io.rs2_ex === io.rd_mem && io.rd_mem =/= 0.U) {
    io.reg2_forward_ex := ForwardingType.ForwardFromMEM
  }.elsewhen(io.reg_wEn_wb && io.rs2_ex === io.rd_wb && io.rd_wb =/= 0.U) {
    io.reg2_forward_ex := ForwardingType.ForwardFromWB
  }.otherwise {
    io.reg2_forward_ex := ForwardingType.NoForward
  }
  /* io.reg1_forward_id */
  when(io.reg_wEn_mem && io.rs1_id === io.rd_mem && io.rd_mem =/= 0.U) {
    io.reg1_forward_id := ForwardingType.ForwardFromMEM
  }.elsewhen(io.reg_wEn_wb && io.rs1_id === io.rd_wb && io.rd_wb =/= 0.U) {
    io.reg1_forward_id := ForwardingType.ForwardFromWB
  }.otherwise {
    io.reg1_forward_id := ForwardingType.NoForward
  }
  /* io.reg2_forward_id */
  when(io.reg_wEn_mem && io.rs2_id === io.rd_mem && io.rd_mem =/= 0.U) {
    io.reg2_forward_id := ForwardingType.ForwardFromMEM
  }.elsewhen(io.reg_wEn_wb && io.rs2_id === io.rd_wb && io.rd_wb =/= 0.U) {
    io.reg2_forward_id := ForwardingType.ForwardFromWB
  }.otherwise {
    io.reg2_forward_id := ForwardingType.NoForward
  }
}

