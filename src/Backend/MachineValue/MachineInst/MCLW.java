package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCLW extends MCInst{
    private Reg rd;
    private Reg rs;
    private int imm;

    public MCLW(Reg rd, Reg rs, int imm) {
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
        useReg.add(rs);
        defReg.add(rd);
    }

    @Override
    public String toString(){
        return "lw " + rd + ", " + imm + "(" + rs + ")";
    }
}
