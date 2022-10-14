package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCMV extends MCInst{
    private Reg rd;
    private Reg rs;

    public MCMV(Reg rd, Reg rs) {
        this.rd = rd;
        this.rs = rs;
        useReg.add(rs);
        defReg.add(rd);
    }

    @Override
    public String toString(){
        return "movz " + rd + ", " + rs + ", $zero";
    }
}
