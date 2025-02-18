package core

import chisel3._

import parameters.System
import parameters.signals.Instructions

class IF2ID extends Module {
  val io = IO(new Bundle {
    val stall = Input(Bool())
    val flush = Input(Bool())
    
    val instruction = Input(UInt(System.InstructionWidth))
    val instAddr    = Input(UInt(System.AddrWidth))

    val output_instruction = Output(UInt(System.DataWidth))
    val output_instAddr    = Output(UInt(System.AddrWidth))
  })

  val instruction = Module(new PipelineRegister(defaultValue = Instructions.nop))
  val instAddr    = Module(new PipelineRegister(defaultValue = System.EntryAddress))

  instruction.io.in := io.instruction
  instAddr.io.in    := io.instAddr

  io.output_instruction := instruction.io.out
  io.output_instAddr    := instAddr.io.out

  instruction.io.stall := io.stall
  instAddr.io.stall    := io.stall

  instruction.io.flush := io.flush
  instAddr.io.flush    := io.flush
}

class ID2EX extends Module {
  val io = IO(new Bundle {
    val flush         = Input(Bool())
    val instruction   = Input(UInt(System.InstructionWidth))
    val instAddr      = Input(UInt(System.AddrWidth))
    val reg1_rAddr    = Input(UInt(System.PhysicalRegisterAddrWidth))
    val reg2_rAddr    = Input(UInt(System.PhysicalRegisterAddrWidth))
    val regs_wEn      = Input(Bool())
    val regs_wAddr    = Input(UInt(System.PhysicalRegisterAddrWidth))
    val regs_wSource  = Input(UInt(2.W))
    val reg1_data     = Input(UInt(System.DataWidth))
    val reg2_data     = Input(UInt(System.DataWidth))
    val immediate     = Input(UInt(System.DataWidth))
    val aluop1_source = Input(UInt(1.W))
    val aluop2_source = Input(UInt(1.W))
    val memory_rEn    = Input(Bool())
    val memory_wEn    = Input(Bool())

    val output_instruction   = Output(UInt(System.DataWidth))
    val output_instAddr      = Output(UInt(System.AddrWidth))
    val output_reg1_rAddr    = Output(UInt(System.PhysicalRegisterAddrWidth))
    val output_reg2_rAddr    = Output(UInt(System.PhysicalRegisterAddrWidth))
    val output_regs_wEn      = Output(Bool())
    val output_regs_wAddr    = Output(UInt(System.PhysicalRegisterAddrWidth))
    val output_regs_wSource  = Output(UInt(2.W))
    val output_reg1_data     = Output(UInt(System.DataWidth))
    val output_reg2_data     = Output(UInt(System.DataWidth))
    val output_immediate     = Output(UInt(System.DataWidth))
    val output_aluop1_source = Output(UInt(1.W))
    val output_aluop2_source = Output(UInt(1.W))
    val output_memory_rEn    = Output(Bool())
    val output_memory_wEn    = Output(Bool())
  })
  val stall = false.B

  val instruction   = Module(new PipelineRegister(defaultValue = Instructions.nop))
  val instAddr      = Module(new PipelineRegister(defaultValue = System.EntryAddress))
  val reg1_rAddr    = Module(new PipelineRegister(System.PhysicalRegisterAddrBits))
  val reg2_rAddr    = Module(new PipelineRegister(System.PhysicalRegisterAddrBits))
  val regs_wEn      = Module(new PipelineRegister(1))
  val regs_wAddr    = Module(new PipelineRegister(System.PhysicalRegisterAddrBits))
  val regs_wSource  = Module(new PipelineRegister(2))
  val reg1_data     = Module(new PipelineRegister())
  val reg2_data     = Module(new PipelineRegister())
  val immediate     = Module(new PipelineRegister())
  val aluop1_source = Module(new PipelineRegister(1))
  val aluop2_source = Module(new PipelineRegister(1))
  val memory_rEn    = Module(new PipelineRegister(1))
  val memory_wEn    = Module(new PipelineRegister(1))

  instruction.io.in   := io.instruction
  instAddr.io.in      := io.instAddr
  reg1_rAddr.io.in    := io.reg1_rAddr
  reg2_rAddr.io.in    := io.reg2_rAddr
  regs_wEn.io.in      := io.regs_wEn
  regs_wAddr.io.in    := io.regs_wAddr
  regs_wSource.io.in  := io.regs_wSource
  reg1_data.io.in     := io.reg1_data
  reg2_data.io.in     := io.reg2_data
  immediate.io.in     := io.immediate
  aluop1_source.io.in := io.aluop1_source
  aluop2_source.io.in := io.aluop2_source
  memory_rEn.io.in    := io.memory_rEn
  memory_wEn.io.in    := io.memory_wEn

  io.output_instruction   := instruction.io.out
  io.output_instAddr      := instAddr.io.out
  io.output_reg1_rAddr    := reg1_rAddr.io.out
  io.output_reg2_rAddr    := reg2_rAddr.io.out
  io.output_regs_wEn      := regs_wEn.io.out
  io.output_regs_wAddr    := regs_wAddr.io.out
  io.output_regs_wSource  := regs_wSource.io.out
  io.output_reg1_data     := reg1_data.io.out
  io.output_reg2_data     := reg2_data.io.out
  io.output_immediate     := immediate.io.out
  io.output_aluop1_source := aluop1_source.io.out
  io.output_aluop2_source := aluop2_source.io.out
  io.output_memory_rEn    := memory_rEn.io.out
  io.output_memory_wEn    := memory_wEn.io.out

  instruction.io.stall   := stall
  instAddr.io.stall      := stall
  reg1_rAddr.io.stall    := stall
  reg2_rAddr.io.stall    := stall
  regs_wEn.io.stall      := stall
  regs_wAddr.io.stall    := stall
  regs_wSource.io.stall  := stall
  reg1_data.io.stall     := stall
  reg2_data.io.stall     := stall
  immediate.io.stall     := stall
  aluop1_source.io.stall := stall
  aluop2_source.io.stall := stall
  memory_rEn.io.stall    := stall
  memory_wEn.io.stall    := stall

  instruction.io.flush   := io.flush
  instAddr.io.flush      := io.flush
  reg1_rAddr.io.flush    := io.flush
  reg2_rAddr.io.flush    := io.flush
  regs_wEn.io.flush      := io.flush
  regs_wAddr.io.flush    := io.flush
  regs_wSource.io.flush  := io.flush
  reg1_data.io.flush     := io.flush
  reg2_data.io.flush     := io.flush
  immediate.io.flush     := io.flush
  aluop1_source.io.flush := io.flush
  aluop2_source.io.flush := io.flush
  memory_rEn.io.flush    := io.flush
  memory_wEn.io.flush    := io.flush
}

class EX2MEM extends Module {
val io = IO(new Bundle() {
    val regs_wEn     = Input(Bool())
    val regs_wSource = Input(UInt(2.W))
    val regs_wAddr   = Input(UInt(System.AddrWidth))
    val instAddr     = Input(UInt(System.AddrWidth))
    val funct3       = Input(UInt(3.W))
    val reg2_data    = Input(UInt(System.DataWidth))
    val memory_rEn   = Input(Bool())
    val memory_wEn   = Input(Bool())
    val alu_result   = Input(UInt(System.DataWidth))

    val output_regs_wEn     = Output(Bool())
    val output_regs_wSource = Output(UInt(2.W))
    val output_regs_wAddr   = Output(UInt(System.AddrWidth))
    val output_instAddr     = Output(UInt(System.AddrWidth))
    val output_funct3       = Output(UInt(System.DataWidth))
    val output_reg2_data    = Output(UInt(System.DataWidth))
    val output_memory_rEn   = Output(Bool())
    val output_memory_wEn   = Output(Bool())
    val output_alu_result   = Output(UInt(System.DataWidth))
  })

  val stall = false.B
  val flush = false.B

  val regs_wEn     = Module(new PipelineRegister(1))
  val regs_wSource = Module(new PipelineRegister(2))
  val regs_wAddr   = Module(new PipelineRegister(System.PhysicalRegisterAddrBits))
  val instAddr     = Module(new PipelineRegister(System.AddrBits))
  val funct3       = Module(new PipelineRegister(3))
  val reg2_data    = Module(new PipelineRegister())
  val alu_result   = Module(new PipelineRegister())
  val memory_rEn   = Module(new PipelineRegister(1))
  val memory_wEn   = Module(new PipelineRegister(1))

  regs_wEn.io.in     := io.regs_wEn
  regs_wSource.io.in := io.regs_wSource
  regs_wAddr.io.in   := io.regs_wAddr
  instAddr.io.in     := io.instAddr
  funct3.io.in       := io.funct3
  reg2_data.io.in    := io.reg2_data
  alu_result.io.in   := io.alu_result
  memory_rEn.io.in   := io.memory_rEn
  memory_wEn.io.in   := io.memory_wEn

  io.output_regs_wEn     := regs_wEn.io.out
  io.output_regs_wSource := regs_wSource.io.out
  io.output_regs_wAddr   := regs_wAddr.io.out
  io.output_instAddr     := instAddr.io.out
  io.output_funct3       := funct3.io.out
  io.output_reg2_data    := reg2_data.io.out
  io.output_alu_result   := alu_result.io.out
  io.output_memory_rEn   := memory_rEn.io.out
  io.output_memory_wEn   := memory_wEn.io.out

  regs_wEn.io.stall     := stall
  regs_wSource.io.stall := stall
  regs_wAddr.io.stall   := stall
  instAddr.io.stall     := stall
  funct3.io.stall       := stall
  reg2_data.io.stall    := stall
  alu_result.io.stall   := stall
  memory_rEn.io.stall   := stall
  memory_wEn.io.stall   := stall

  regs_wEn.io.flush     := flush
  regs_wSource.io.flush := flush
  regs_wAddr.io.flush   := flush
  instAddr.io.flush     := flush
  funct3.io.flush       := flush
  reg2_data.io.flush    := flush
  alu_result.io.flush   := flush
  memory_rEn.io.flush   := flush
  memory_wEn.io.flush   := flush
}

class MEM2WB extends Module {
  val io = IO(new Bundle() {
    val instAddr     = Input(UInt(System.AddrWidth))
    val alu_result   = Input(UInt(System.DataWidth))
    val regs_wEn     = Input(Bool())
    val regs_wSource = Input(UInt(2.W))
    val regs_wAddr   = Input(UInt(System.AddrWidth))
    val memory_rData = Input(UInt(System.DataWidth))

    val output_instAddr     = Output(UInt(System.AddrWidth))
    val output_alu_result   = Output(UInt(System.DataWidth))
    val output_regs_wEn     = Output(Bool())
    val output_regs_wSource = Output(UInt(2.W))
    val output_regs_wAddr   = Output(UInt(System.AddrWidth))
    val output_memory_rData = Output(UInt(System.DataWidth))
  })
  val stall = false.B
  val flush = false.B

  val alu_result   = Module(new PipelineRegister())
  val memory_rData = Module(new PipelineRegister())
  val regs_wEn     = Module(new PipelineRegister(1))
  val regs_wSource = Module(new PipelineRegister(2))
  val regs_wAddr   = Module(new PipelineRegister(System.PhysicalRegisterAddrBits))
  val instAddr     = Module(new PipelineRegister(System.InstructionBits))

  alu_result.io.in   := io.alu_result
  memory_rData.io.in := io.memory_rData
  regs_wEn.io.in     := io.regs_wEn
  regs_wSource.io.in := io.regs_wSource
  regs_wAddr.io.in   := io.regs_wAddr
  instAddr.io.in     := io.instAddr

  io.output_alu_result   := alu_result.io.out
  io.output_memory_rData := memory_rData.io.out
  io.output_regs_wEn     := regs_wEn.io.out
  io.output_regs_wSource := regs_wSource.io.out
  io.output_regs_wAddr   := regs_wAddr.io.out
  io.output_instAddr     := instAddr.io.out

  alu_result.io.stall   := stall
  memory_rData.io.stall := stall
  regs_wEn.io.stall     := stall
  regs_wSource.io.stall := stall
  regs_wAddr.io.stall   := stall
  instAddr.io.stall     := stall

  alu_result.io.flush   := flush
  memory_rData.io.flush := flush
  regs_wEn.io.flush     := flush
  regs_wSource.io.flush := flush
  regs_wAddr.io.flush   := flush
  instAddr.io.flush     := flush
}
