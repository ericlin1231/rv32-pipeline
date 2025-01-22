package core

import chisel3._

import bundle.InstructionROMBundle
import bundle.FetchDebugBundle
import parameters.System
import parameters.signals.Instructions

class Fetch extends Module {
  val io = IO(new Bundle {
    val instValid   = Input(Bool())
    val jump_flag   = Input(Bool())
    val jump_addr   = Input(UInt(System.AddrWidth))

    val instAddr    = Output(UInt(System.AddrWidth))
    val instruction = Output(UInt(System.InstructionWidth))

    val IROMPort  = Flipped(new InstructionROMBundle)
    val DebugPort = new FetchDebugBundle
  })
  
  val PC = RegInit(System.EntryAddress)
  io.IROMPort.address := System.EntryAddress

  PC := MuxCase(
    PC + 4.U,
    IndexedSeq(
      (io.jump_flag && !io.stall_flag) -> io.jump_addr,
      (io.stall_flag || !io.instValid) -> PC
    )
  )

  io.instAddr := PC
  io.instruction := Mux(instValid, io.IROMPort.instruction, Instructions.nop)

  /* Fetch Debug */
  io.DebugPort.debug_read_pc := PC
  io.DebugPort.debug_read_instruction := Mux(instValid, io.IROMPort.instruction, Instructions.nop)
}
