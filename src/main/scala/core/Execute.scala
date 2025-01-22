package core

import chisel3._
import chisel3.util.Cat
import chisel3.util.MuxLookup

import parameters.System
import parameters.signals.Opcode
import parameters.signals.Funct3TypeB
import parameters.signals.ALUOp1Source
import parameters.signals.ALUOp2Source

class Execute extends Module {
  val io = IO(new Bundle {
    val instruction       = Input(UInt(System.InstructionWidth))
    val instAddr          = Input(UInt(System.AddrWidth))
    val reg1_data         = Input(UInt(System.DataWidth))
    val reg2_data         = Input(UInt(System.DataWidth))
    val immediate         = Input(UInt(System.DataWidth))
    val aluop1_source     = Input(UInt(1.W))
    val aluop2_source     = Input(UInt(1.W))
    val MEM_forward_data  = Input(UInt(System.DataWidth))
    val WB_forward_data   = Input(UInt(System.DataWidth))
    val reg1_forward_type = Input(UInt(2.W))
    val reg2_forward_type = Input(UInt(2.W))

    val alu_result = Output(UInt(System.DataWidth))
    val reg2_data  = Output(UInt(System.DataWidth))
  })

  val opcode = io.instruction(6, 0)
  val funct3 = io.instruction(14, 12)
  val funct7 = io.instruction(31, 25)
  val uimm   = io.instruction(19, 15)

  val alu      = Module(new ALU)
  val alu_ctrl = Module(new ALUControl)

  alu_ctrl.io.opcode := opcode
  alu_ctrl.io.funct3 := funct3
  alu_ctrl.io.funct7 := funct7
  alu.io.func        := alu_ctrl.io.alu_funct

  val reg1_data = MuxLookup(
    io.reg1_forward,
    io.reg1_data,
    IndexedSeq(
      ForwardingType.ForwardFromMEM -> io.forward_from_mem,
      ForwardingType.ForwardFromWB  -> io.forward_from_wb
    )
  )
  alu.io.op1 := Mux(
    io.aluop1_source === ALUOp1Source.InstructionAddress,
    io.instruction_address,
    reg1_data
  )

  val reg2_data = MuxLookup(
    io.reg2_forward,
    io.reg2_data,
    IndexedSeq(
      ForwardingType.ForwardFromMEM -> io.forward_from_mem,
      ForwardingType.ForwardFromWB  -> io.forward_from_wb
    )
  )
  alu.io.op2 := Mux(
    io.aluop2_source === ALUOp2Source.Immediate,
    io.immediate,
    reg2_data
  )
  io.mem_alu_result := alu.io.result
  io.mem_reg2_data  := reg2_data
}
