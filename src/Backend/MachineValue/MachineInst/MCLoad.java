package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCLoad extends MCInst{
    private Reg rd;
    private Reg rs;
    private int imm;

    public MCLoad(Reg rd, Reg rs, int imm) {
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
    }

    @Override
    public String toString(){
        return "lw " + rs + ", " + imm + "(" + rd + ")";
    }
}
