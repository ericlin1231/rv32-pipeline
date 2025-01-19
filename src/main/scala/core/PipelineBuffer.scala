package core

import chisel3._

import bundle.PipelineBufferRegisterBundle
import parameters.System
import parameters.signals.Instructions

class IF2ID extends Module {
  val io = IO(new Bundle {
    val Port                = new PipelineBufferRegisterBundle
    
    val instruction         = Input(UInt(System.InstructionWidth))
    val instruction_address = Input(UInt(System.AddrWidth))

    val output_instruction         = Output(UInt(System.DataWidth))
    val output_instruction_address = Output(UInt(System.AddrWidth))
  })

  val instruction = Module(new PipelineRegister(defaultValue = Instructions.nop))
  val instruction_address = Module(new PipelineRegister(defaultValue = System.EntryAddress))

  io.Port <> instruction.io.Port
  io.Port <> instruction_address.io.Port

  io.output_instruction         := instruction.io.out
  io.output_instruction_address := instruction_address.io.out

  instruction.io.in         := io.instruction
  instruction_address.io.in := io.instruction_address
}

// class ID2EX extends Module {

// }

// class EX2MEM extends Module {

// }

// class MEM2WB extends Module {

// }
