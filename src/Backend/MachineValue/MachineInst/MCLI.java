package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCLI extends MCInst{
    private int imm;
    private Reg reg;

    public MCLI(Reg reg, int imm){
        this.reg = reg;
        this.imm = imm;
    }

    @Override
    public String toString(){
        return "li " + reg + " " + imm;
    }
}
