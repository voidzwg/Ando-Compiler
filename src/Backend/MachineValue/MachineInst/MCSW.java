package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCSW extends MCInst{
    private Reg value;
    private Reg pointer;
    private int offset;

    public MCSW(Reg value, Reg pointer, int offset){
        this.value = value;
        this.pointer = pointer;
        this.offset = offset;
    }

    @Override
    public String toString(){
        return "sw " + value + ", " + offset + "(" + pointer + ")";
    }
}
