package Backend.MachineValue.MachineInst;

import Backend.Reg.VirtualReg;

public class MCLi extends MCInst{
    private int imm;
    private VirtualReg vReg;

    public MCLi(VirtualReg vReg, int imm){
        this.vReg = vReg;
        this.imm = imm;
    }

    @Override
    public String toString(){
        return "li " + vReg + " " + imm;
    }
}
