package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCLI extends MCInst{
    private int imm;
    private Reg rd;

    public MCLI(Reg rd, int imm){
        this.rd = rd;
        this.imm = imm;
        defReg.add(rd);
    }

    @Override
    public String toString(){
        return "li " + rd + ", " + imm;
    }
}
