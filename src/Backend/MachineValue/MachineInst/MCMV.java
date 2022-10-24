package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCMV extends MCInst{

    public MCMV(Reg rd, Reg rs1) {
        this.rd = rd;
        this.rs1 = rs1;
        useReg.add(rs1);
        defReg.add(rd);
    }

    @Override
    public String toString(){
        return "move " + rd + ", " + rs1;
    }

    public Reg getDst() {
        return rd;
    }

    public Reg getSrc() {
        return rs1;
    }
}
