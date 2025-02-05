package core

import scala.collection.immutable.ArraySeq

import chisel3._
import chisel3.util._

import parameters.System
import parameters.signals.Opcode
import parameters.signals.ALUOp1Source
import parameters.signals.ALUOp2Source
import parameters.signals.RegWriteSource

class Decode extends Module {
  val io = IO(new Bundle {
    val instruction       = Input(UInt(System.InstructionWidth))
    val instAddr          = Input(UInt(System.AddrWidth))
    val reg1_data         = Input(UInt(System.DataWidth))
    val reg2_data         = Input(UInt(System.DataWidth))
    val MEM_forward_data  = Input(UInt(System.DataWidth))
    val WB_forward_data   = Input(UInt(System.DataWidth))
    val reg1_forward_type = Input(UInt(2.W))
    val reg2_forward_type = Input(UInt(2.W))

    /* Signals Pass to EX for ALU Operation */
    val EX_immediate      = Output(UInt(System.DataWidth))
    val EX_aluop1_source  = Output(UInt(1.W))
    val EX_aluop2_source  = Output(UInt(1.W))

    /* Jump Signals Pass to IF to Change PC */
    val IF_jump_flag      = Output(Bool())
    val IF_jump_addr      = Output(UInt(System.AddrWidth))

    /* Signals Pass to MEM and WB */
    val PASS_MEM_rEn      = Output(Bool())
    val PASS_MEM_wEn      = Output(Bool())
    val PASS_REGS_wSource = Output(UInt(2.W))
    val PASS_REGS_wEn     = Output(Bool())
    val PASS_REGS_wAddr   = Output(UInt(System.PhysicalRegisterAddrWidth))

    /* Signals Pass to Control Unit to Determine Stall or Flush */
    val CTRL_ID_jump      = Output(Bool())
    val CTRL_REGS_rAddr1  = Output(UInt(System.PhysicalRegisterAddrWidth))
    val CTRL_REGS_rAddr2  = Output(UInt(System.PhysicalRegisterAddrWidth))
  })
  val opcode = io.instruction(6, 0)
  val funct3 = io.instruction(14, 12)
  val funct7 = io.instruction(31, 25)
  val rd     = io.instruction(11, 7)
  val rs1    = io.instruction(19, 15)
  val rs2    = io.instruction(24, 20)
  
  /* Control Signals */
  io.CTRL_REGS_rAddr1 := Mux(opcode === Instructions.lui, 0.U(System.PhysicalRegisterAddrWidth), rs1)
  io.CTRL_REGS_rAddr2 := rs2
  io.CTRL_ID_jump     := ((opcode === Opcode.B)
                          || (opcode === Opcode.jal)
                          || (opcode === Opcode.jalr))
  
  /* Execute Signals */
  io.EX_immediate := MuxLookup(
    opcode,
    Cat(Fill(20, io.instruction(31)), io.instruction(31, 20)),
    IndexedSeq(
      Opcode.I -> Cat(Fill(21, io.instruction(31)), io.instruction(30, 20)),
      Opcode.L -> Cat(Fill(21, io.instruction(31)), io.instruction(30, 20)),
      Opcode.jalr  -> Cat(Fill(21, io.instruction(31)), io.instruction(30, 20)),
      Opcode.S -> Cat(Fill(21, io.instruction(31)), io.instruction(30, 25), io.instruction(11, 7)),
      Opcode.B -> Cat(
        Fill(20, io.instruction(31)),
        io.instruction(7),
        io.instruction(30, 25),
        io.instruction(11, 8),
        0.U(1.W)
      ),
      Opcode.lui   -> Cat(io.instruction(31, 12), 0.U(12.W)),
      Opcode.auipc -> Cat(io.instruction(31, 12), 0.U(12.W)),
      Opcode.jal -> Cat(
        Fill(12, io.instruction(31)),
        io.instruction(19, 12),
        io.instruction(20),
        io.instruction(30, 21),
        0.U(1.W)
      )
    )
  )
  io.EX_aluop1_source := Mux(
    opcode === Opcode.auipc || opcode === Opcode.B || opcode === Opcode.jal,
    ALUOp1Source.InstructionAddress,
    ALUOp1Source.Register
  )
  io.EX_aluop2_source := Mux(
    opcode === Opcode.RM,
    ALUOp2Source.Register,
    ALUOp2Source.Immediate
  )

  /* Memory Signals */
  io.PASS_MEM_rEn := opcode === Opcode.L
  io.PASS_MEM_wEn := opcode === Opcode.S

  /* WriteBack Signals */
  io.PASS_REGS_wEn := ((opcode === Opcode.RM)
                       || (opcode === Opcode.I)
                       || (opcode === Opcode.L)
                       || (opcode === Opcode.auipc)
                       || (opcode === Opcode.lui)
                       || (opcode === Opcode.jal)
                       || (opcode === Opcode.jalr))

  io.PASS_REGS_wSource := MuxLookup(
    opcode,
    RegWriteSource.ALUResult,
    IndexedSeq(
      Opcode.L -> RegWriteSource.Memory,
      // Instructions.csr   -> RegWriteSource.CSR,
      Opcode.jal   -> RegWriteSource.NextInstructionAddress,
      Opcode.jalr  -> RegWriteSource.NextInstructionAddress
    )
  )

  io.PASS_REGS_wAddr := rd

  /* Select Registers Data Source */
  val reg1_data = MuxLookup(
    io.reg1_forward_type,
    0.U,
    IndexedSeq(
      ForwardingType.NoForward      -> (io.reg1_data),
      ForwardingType.ForwardFromWB  -> (io.WB_forward_data),
      ForwardingType.ForwardFromMEM -> (io.MEM_forward_data)
    )
  )
  val reg2_data = MuxLookup(
    io.reg2_forward_type,
    0.U,
    IndexedSeq(
      ForwardingType.NoForward      -> (io.reg2_data),
      ForwardingType.ForwardFromWB  -> (io.WB_forward_data),
      ForwardingType.ForwardFromMEM -> (io.MEM_forward_data)
    )
  )

  /* Instruction Fetch Signals */
  io.IF_jump_flag := ((opcode === Instructions.jal)
                      || (opcode === Instructions.jalr)
                      || ((opcode === Opcode.B)
                          && MuxLookup(
                               funct3,
                               false.B,
                               IndexedSeq(
                                 InstructionsTypeB.beq  -> (reg1_data === reg2_data),
                                 InstructionsTypeB.bne  -> (reg1_data =/= reg2_data),
                                 InstructionsTypeB.blt  -> (reg1_data.asSInt < reg2_data.asSInt),
                                 InstructionsTypeB.bge  -> (reg1_data.asSInt >= reg2_data.asSInt),
                                 InstructionsTypeB.bltu -> (reg1_data.asUInt < reg2_data.asUInt),
                                 InstructionsTypeB.bgeu -> (reg1_data.asUInt >= reg2_data.asUInt)
                               )
                             )))

  io.IF_jump_addr := MuxLookup(
                       opcode,
                       0.U,
                       IndexedSeq(
                         Opcode.B    -> (io.instAddr + io.EX_immediate),
                         Opcode.jal  -> (io.instAddr + io.EX_immediate),
                         Opcode.jalr -> (reg1_data + io.EX_immediate)
                       )
                     )
}
