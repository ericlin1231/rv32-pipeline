package core

import chisel3._
import chisel3.util._

import parameters.System
import parameters.signals.RegWriteSource

class WriteBack extends Module {
  val io = IO(new Bundle() {
    val instAddr     = Input(UInt(System.AddrWidth))
    val alu_result   = Input(UInt(System.DataWidth))
    val MEM_rData    = Input(UInt(System.DataWidth))
    val REGS_wSource = Input(UInt(2.W))

    val REGS_wData = Output(UInt(System.DataWidth))
  })
  io.REGS_wData := MuxLookup(
    io.REGS_wSource,
    io.alu_result,
    IndexedSeq(
      RegWriteSource.Memory                 -> io.MEM_rData
      // RegWriteSource.CSR                    -> io.csr_read_data,
      RegWriteSource.NextInstructionAddress -> (io.instAddr + 4.U)
    )
  )
}
