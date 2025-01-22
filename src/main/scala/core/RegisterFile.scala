package core

import chisel3._

import parameters.System
import parameters.signals.Registers
import bundle.RegisterDebugBundle

class RegisterFile extends Module {
  val io = IO(new Bundle {
    val wEn   = Input(Bool())
    val wAddr = Input(UInt(System.PhysicalRegisterAddrWidth))
    val wData = Input(UInt(System.DataWidth))

    val rAddr1 = Input(UInt(System.PhysicalRegisterAddrWidth))
    val rAddr2 = Input(UInt(System.PhysicalRegisterAddrWidth))
    val rData1 = Output(UInt(System.DataWidth))
    val rData2 = Output(UInt(System.DataWidth))

    val DebugPort     = new RegisterDebugBundle
  })

  val registers = RegInit(VecInit(Seq.fill(System.PhysicalRegisters)(0.U(System.DataWidth))))

  when(!reset.asBool) {
    when(io.wEn && io.wAddr =/= 0.U) {
      registers(io.wAddr) := io.wData
    }
  }

  io.rData1 := Mux(
    io.rAddr1 === 0.U,
    0.U,
    registers(io.rAddr1)
  )

  io.rData2 := Mux(
    io.rAddr2 === 0.U,
    0.U,
    registers(io.rAddr2)
  )

  io.DebugPort.debug_rData := Mux(
    io.DebugPort.debug_rAddr === 0.U,
    0.U,
    registers(io.DebugPort.debug_rAddr)
  )
}
