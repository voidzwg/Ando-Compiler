package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCSW extends MCInst{
    private Reg rs1;
    private Reg rs2;
    private int offset;

    //  rs1为value, rs2为pointer
    public MCSW(Reg rs1, Reg rs2, int offset){
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.offset = offset;
        useReg.add(rs1);
        useReg.add(rs2);
    }

    @Override
    public String toString(){
        return "sw " + rs1 + ", " + offset + "(" + rs2 + ")";
    }
}
