package core

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import parameters.System
import parameters.signals.Instructions

class IF2IDTest extends AnyFlatSpec with ChiselScalatestTester {
  "clean data when flush and stop update data when stall" should "pass" in {
    test(new IF2ID) { c =>
      c.io.stall.poke(false.B)
      c.io.flush.poke(false.B)
      c.io.instruction.poke(0x12345678L.U)
      c.io.instAddr.poke(0x12345678L.U)
      c.clock.step()

      c.io.output_instruction.expect(0x12345678L.U)
      c.io.output_instAddr.expect(0x12345678L.U)

      c.io.stall.poke(true.B)
      c.io.flush.poke(false.B)
      c.io.instruction.poke(0x87654321L.U)
      c.io.instAddr.poke(0x87654321L.U)
      c.clock.step()


      c.io.output_instruction.expect(0x12345678L.U)
      c.io.output_instAddr.expect(0x12345678L.U)

      c.io.stall.poke(false.B)
      c.io.flush.poke(true.B)
      c.io.instruction.poke(0x87654321L.U)
      c.io.instAddr.poke(0x87654321L.U)
      c.clock.step()
      
      c.io.output_instruction.expect(Instructions.nop)
      c.io.output_instAddr.expect(System.EntryAddress)
    }
  }
}
