package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCLW extends MCInst{
    private int imm;

    public MCLW(Reg rd, Reg rs1, int imm) {
        this.rd = rd;
        this.rs1 = rs1;
        this.imm = imm;
        useReg.add(rs1);
        defReg.add(rd);
    }

    @Override
    public String toString(){
        return "lw " + rd + ", " + imm + "(" + rs1 + ")";
    }
}
