package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCLoad extends MCInst{
    private int imm;
    private Reg rd;
    private String msgName;
    private int type;

    //  li
    public MCLoad(Reg rd, int imm){
        this.rd = rd;
        this.imm = imm;
        this.type = 0;
        defReg.add(rd);
    }

    //  la
    public MCLoad(Reg rd, String msgName){
        this.msgName = msgName;
        this.rd = rd;
        this.type = 1;
        defReg.add(rd);
    }

    @Override
    public String toString(){
        if(type == 0) return "li " + rd + ", " + imm;
        else return "la " + rd + ", " + msgName;
    }
}
