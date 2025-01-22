package core

import chisel3._

import bundle.CPUBundle
import parameters.System
import peripheral.Memory
import peripheral.InstructionROM

class CPU(filename: String) extends Module {
  /* Current Instruction Validity Control and Debug Port */
  val io   = IO(new CPUBundle)

  /* Pipeline Register and Read Only Memory */
  val REGS = Module(new RegisterFile)
  val IROM = Module(new InstructionROM(filename))

  /* Pipeline Controller */
  val CTRL    = Module(new Control)
  val FORWARD = Module(new Forwarding)

  /* Pipeline Component */
  val IF     = Module(new Fetch)
  val IF2ID  = Module(new IF2ID)
  val ID     = Module(new Decode)
  val ID2EX  = Module(new ID2EX)
  val EX     = Module(new Execute)
  val EX2MEM = Module(new EX2MEM)
  val MEM    = Module(new Memory)
  val MEM2WB = Module(new MEM2WB)
  val WB     = Module(new WriteBack)

  /* Debug */
  io.IROMDebug <> IROM.io.DebugPort
  io.REGSDebug <> REGS.io.DebugPort
  io.IFDebug   <> IF.io.DebugPort
  io.MEMDebug  <> MEM.io.DebugPort

  /*
   * The name convention is the TARGET_SIGNAL
   * For example, the REGS_wData is the data
   * that want to write to the register file
   * While the out prefix  is meaning the signal
   * is from the pipeline buffer
   */

  /* Register File */
  REGS.io.wEn    := MEM2WB.io.out_REGS_wEn
  REGS.io.wAddr  := MEM2WB.io.out_REGS_wAddr
  REGS.io.wData  := WB.io.REGS_wData
  REGS.io.rAddr1 := ID.io.REGS_rAddr1
  REGS.io.rAddr2 := ID.io.REGS_rAddr2

  /* Control */
  /*
   * For instruction fetch, the control unit know
   * the next instruction should jump or not, the
   * information is from instruction decode stage
   * 
   * The control unit need to know the current instruction
   * at decode will use which source registers, to check
   * there has data hazard or not, the data hazard has two
   * situation need to stall or flush
   * 1. EX will write the register which is the source register
   *    for the instruction at ID
   * 2. MEM will write the register which is the source register
   *    for the instruction at ID
   *
   * The branch taken or non-taken resolved at ID stage, the 
   * IF_jump_flag determine the PC shoud jump to the IF_jump_addr
   * which can found at IF section, compare with the CPU that resolve
   * the branch condition at EX stage, this implementation calculate
   * the branch condition result at ID stage, compare with the implementation
   * that resolve the branch condition result at EX stage, can increase performance
   */
  CTRL.io.IF_jump_flag := ID.io.CTRL_IF_jump_flag
  CTRL.io.ID_jump      := ID.io.CTRL_ID_jump
  CTRL.io.REGS_rAddr1  := ID.io.CTRL_REGS_rAddr1
  CTRL.io.REGS_rAddr2  := ID.io.CTRL_REGS_rAddr2
  CTRL.io.EX_MEM_rEn   := ID2EX.io.out_CTRL_MEM_rEn
  CTRL.io.EX_rd        := ID2EX.io.out_CTRL_REGS_wAddr
  CTRL.io.MEM_MEM_rEn  := EX2MEM.io.out_CTRL_MEM_rEn
  CTRL.io.MEM_rd       := EX2MEM.io.out_CTRL_REGS_wAddr

  /* Forward */
  /*
   * The forward unit know each instruction registers
   * dependency, if there has no dependency, the instruction
   * can use the data read from the register file, but if there
   * has data hazard, meaning that the previous instruction 
   * register destination (rd) is same as the next instruction
   * source register, so that there has three types of forward signal
   * 1. NoForward: No instruction dependency, use the data read
   *               from register file
   * 2. ForwardFromMEM: The `arithmetic` instruction at EX stage use the
   *                    source register for next instruction as register destination,
   *                    cache the result at EX2MEM, then forward the data at MEM stage
   * 3. ForwardFromWB: The `Load` (L-Type) instruction at MEM  stage use the
   *                   source register for next instruction as register destination,
   *                   while the `Load` instruction must get the result at MEM stage,
   *                   the data from memory cached at MEM2WB, then forward the data at WB stage
   */
  FORWARD.io.ID_rs1       := ID.io.FORWARD_REGS_rAddr1
  FORWARD.io.ID_rs2       := ID.io.FORWARD_REGS_rAddr2
  FORWARD.io.EX_rs1       := ID2EX.io.out_FORWARD_REGS_rAddr1
  FORWARD.io.EX_rs2       := ID2EX.io.out_FORWARD_REGS_rAddr2
  FORWARD.io.MEM_rd       := EX2MEM.io.out_FORWARD_REGS_wAddr
  FORWARD.io.MEM_REGS_wEn := EX2MEM.io.out_FORWARD_REGS_wEn
  FORWARD.io.WB_rd        := MEM2WB.io.out_FORWARD_REGS_wAddr
  FORWARD.io.WB_REGS_wEn  := MEM2WB.io.out_FORWARD_REGS_wEn

  /* Instruction Fetch */
  IF.io.instValid  := io.instValid
  IF.io.stall_flag := CTRL.io.IF_stall_flag
  IF.io.jump_flag  := ID.io.IF_jump_flag
  IF.io.jump_addr  := ID.io.IF_jump_addr
  IF.io.IROMPort   <> IROM.io.IROMPort
  
  /* Instruction Fetch & Instruction Decode Pipeline Buffer */
  IF2ID.io.stall       := CTRL.io.IF_stall
  IF2ID.io.flush       := CTRL.io.IF_flush
  IF2ID.io.instruction := IF.io.instruction
  IF2ID.io.instAddr    := IF.io.instAddr

  /* Instruction Decoder */
  /*
   * There has 3 register's data resources
   * 1. Register File
   * 2. Forward (Bypass) from MEM stage due to data hazard
   * 3. Forward (Bypass) from WB stage due to data hazard
   */
  ID.io.instruction       := IF2ID.io.out_instruction
  ID.io.instAddr          := IF2ID.io.out_instAddr
  ID.io.reg1_data         := REGS.io.rData1
  ID.io.reg2_data         := REGS.io.rData2
  ID.io.MEM_forward_data  := MEM.io.forward_data
  ID.io.WB_forward_data   := WB.io.forward_data /* same as data write to register file */
  ID.io.reg1_forward_type := Forward.io.ID_reg1_forward_type
  ID.io.reg2_forward_type := Forward.io.ID_reg2_forward_type

  /* Instruction Decode & Execute Pipeline Buffer */
  /*
   * The PASS prefix mean the signal is not for next stage
   * It is a signal cache for current instruction
   * But the signal is actually used to provide the information
   * relate to current instruction, the information used to
   * detect hazard, for the control unit to determine should
   * stall or flush, for the WriteBack stage to know which data
   * source will write to the register file
   *
   * The CTRL prefix mean the signal is for control unit
   * While the pipeline may occur data hazard
   * Use the register's address (or identifier) to check
   * the next instruction source registers is depend on
   * current instruction or not
   */
  ID2EX.io.flush         := CTRL.io.ID_flush
  ID2EX.io.instruction   := IF2ID.io.out_instruction
  ID2EX.io.instAddr      := IF2ID.io.out_instAddr
  ID2EX.io.reg1_data     := REGS.io.rData1
  ID2EX.io.reg2_data     := REGS.io.rData2
  ID2EX.io.immediate     := ID.io.EX_immediate
  ID2EX.io.aluop1_source := ID.io.EX_aluop1_source
  ID2EX.io.aluop2_source := ID.io.EX_aluop2_source
  
  ID2EX.io.PASS_REGS_wEn     := ID.io.PASS_REGS_wEn
  ID2EX.io.PASS_REGS_wAddr   := ID.io.PASS_REGS_wAddr
  ID2EX.io.PASS_REGS_wSource := ID.io.PASS_REGS_wSource
  ID2EX.io.PASS_MEM_rEn      := ID.io.PASS_MEM_rEn
  ID2EX.io.PASS_MEM_wEn      := ID.io.PASS_MEM_wEn

  ID2EX.io.CTRL_REGS_rAddr1 := ID.io.CTRL_REGS_rAddr1
  ID2EX.io.CTRL_REGS_rAddr2 := ID.io.CTRL_REGS_rAddr2

  /* Execution */
  /*
   * There has 3 register's data resources
   * 1. Register File
   * 2. Forward (Bypass) from MEM stage due to data hazard
   * 3. Forward (Bypass) from WB stage due to data hazard
   */
  EX.io.instruction       := ID2EX.io.out_instruction
  EX.io.instAddr          := ID2EX.io.out_instAddr
  EX.io.reg1_data         := ID2EX.io.out_reg1_data
  EX.io.reg2_data         := ID2EX.io.out_reg2_data
  EX.io.immediate         := ID2EX.io.out_immediate
  EX.io.aluop1_source     := ID2EX.io.out_aluop1_source
  EX.io.aluop2_source     := ID2EX.io.out_aluop2_source
  EX.io.MEM_forward_data  := MEM.io.forward_data
  EX.io.WB_forward_data   := WB.io.forward_data
  EX.io.reg1_forward_type := Forward.io.EX_reg1_forward_type
  EX.io.reg2_forward_type := Forward.io.EX_reg2_forward_type

  /* Execute & Memory Pipeline Buffer */
  /*
   * The PASS prefix meaning that the signals is not 
   * used in MEM stage, the signals for register file
   * is used at WriteBack Stage
   *
   * The ALU result from EX stage has 2 usage, depend on memory
   * read/write or register write signal is enable, so the ALU
   * result will send to MEM stage also to MEM2WB buffer
   * 1. Memory address
   * 2. Data write to the register destination (rd)
   *
   * For the S-type instruction, the data has 3 sources
   * 1. Forward from MEM stage, due to the source register has
   *    dependency to previous arithmetic instruction used the
   *    register as destination register
   * 2. Forward from WB stage, due to the source register has
   *    dependency to previous L-type instruction used the
   *    register as destination register
   * 3. No dependency, use the data from register file directly
   */
  EX2MEM.io.PASS_REGS_wEn     := ID2EX.io.out_PASS_REGS_wEn
  EX2MEM.io.PASS_REGS_wSource := ID2EX.io.out_PASS_REGS_wSource
  EX2MEM.io.PASS_REGS_wAddr   := ID2EX.io.out_PASS_REGS_wAddr

  EX2MEM.io.instAddr   := ID2EX.io.out_instAddr
  EX2MEM.io.funct3     := ID2EX.io.out_instruction(14, 12)
  EX2MEM.io.alu_result := EX.io.alu_result
  EX2MEM.io.MEM_rEn    := ID2EX.io.out_PASS_MEM_rEn
  EX2MEM.io.MEM_wEn    := ID2EX.io.out_PASS_MEM_wEn
  EX2MEM.io.MEM_wData  := EX.io.MEM_reg2_data

  /* Memory */
  MEM.io.MEMPort.address := EX2MEM.io.out_alu_result
  MEM.io.MEMPort.rEn     := EX2MEM.io.out_MEM_rEn
  MEM.io.MEMPort.wEn     := EX2MEM.io.out_MEM_wEn
  MEM.io.MEMPort.wData   := EX2MEM.io.out_MEM_wData
  MEM.io.wSource         := EX2MEM.io.out_MEM_wSource
  MEM.io.funct3          := EX2MEM.io.out_funct3

  /* Memory & WriteBack Pipeline Buffer */
  MEM2WB.io.instAddr  := EX2MEM.io.out_instAddr
  MEM2WB.io.rd_wData  := EX2MEM.io.alu_result
  MEM2WB.io.MEM_rData := MEM.io.MEMPort.rData

  MEM2WB.io.REGS_wEn     := EX2MEM.io.out_PASS_REGS_wEn
  MEM2WB.io.REGS_wSource := EX2MEM.io.out_PASS_REGS_wSource
  MEM2WB.io.REGS_wAddr   := EX2MEM.io.out_PASS_REGS_wAddr

  /* Write Back */
  WB.io.instAddr     := MEM2WB.io.out_instAddr
  WB.io.rd_wData     := MEM2WB.io.out_rd_wData
  WB.io.REGS_wData   := MEM2WB.io.out_MEM_rData
  WB.io.REGS_wSource := MEM2WB.io.out_REGS_wSource
}
