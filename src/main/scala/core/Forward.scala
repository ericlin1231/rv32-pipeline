package core

import chisel3._

import parameters.System
import parameters.signals.ForwardingType

class Forwarding extends Module {
  val io = IO(new Bundle() {
    val ID_rs1       = Input(UInt(System.PhysicalRegisterAddrWidth))
    val ID_rs2       = Input(UInt(System.PhysicalRegisterAddrWidth))
    val EX_rs1       = Input(UInt(System.PhysicalRegisterAddrWidth))
    val EX_rs2       = Input(UInt(System.PhysicalRegisterAddrWidth))
    val MEM_rd       = Input(UInt(System.PhysicalRegisterAddrWidth))
    val MEM_REGS_wEn = Input(Bool())                                    
    val WB_rd        = Input(UInt(System.PhysicalRegisterAddrWidth))
    val WB_REGS_wEn  = Input(Bool())                                    

    val ID_reg1_forward_type = Output(UInt(2.W))
    val ID_reg2_forward_type = Output(UInt(2.W))
    val EX_reg1_forward_type = Output(UInt(2.W))
    val EX_reg2_forward_type = Output(UInt(2.W))
  })

  when(io.MEM_REGS_wEn && io.EX_rs1 === io.MEM_rd && io.MEM_rd =/= 0.U) {
    io.EX_reg1_forward_type := ForwardingType.ForwardFromMEM
  }.elsewhen(io.WB_REGS_wEn && io.EX_rs1 === io.WB_rd && io.WB_rd =/= 0.U) {
    io.EX_reg1_forward_type := ForwardingType.ForwardFromWB
  }.otherwise {
    io.EX_reg1_forward_type := ForwardingType.NoForward
  }
  when(io.MEM_REGS_wEn && io.EX_rs2 === io.MEM_rd && io.MEM_rd =/= 0.U) {
    io.EX_reg2_forward_type := ForwardingType.ForwardFromMEM
  }.elsewhen(io.WB_REGS_wEn && io.EX_rs2 === io.WB_rd && io.WB_rd =/= 0.U) {
    io.EX_reg2_forward_type := ForwardingType.ForwardFromWB
  }.otherwise {
    io.EX_reg2_forward_type := ForwardingType.NoForward
  }
  when(io.MEM_REGS_wEn && io.ID_rs1 === io.MEM_rd && io.MEM_rd =/= 0.U) {
    io.ID_reg1_forward_type := ForwardingType.ForwardFromMEM
  }.elsewhen(io.WB_REGS_wEn && io.ID_rs1 === io.WB_rd && io.WB_rd =/= 0.U) {
    io.ID_reg1_forward_type := ForwardingType.ForwardFromWB
  }.otherwise {
    io.ID_reg1_forward_type := ForwardingType.NoForward
  }
  when(io.MEM_REGS_wEn && io.ID_rs2 === io.MEM_rd && io.MEM_rd =/= 0.U) {
    io.ID_reg2_forward_type := ForwardingType.ForwardFromMEM
  }.elsewhen(io.WB_REGS_wEn && io.ID_rs2 === io.WB_rd && io.WB_rd =/= 0.U) {
    io.ID_reg2_forward_type := ForwardingType.ForwardFromWB
  }.otherwise {
    io.ID_reg2_forward_type := ForwardingType.NoForward
  }
}

