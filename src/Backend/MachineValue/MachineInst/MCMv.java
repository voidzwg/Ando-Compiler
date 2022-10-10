package Backend.MachineValue.MachineInst;

import Backend.Reg.VirtualReg;

public class MCMv extends MCInst{
    private VirtualReg rd;
    private VirtualReg rs;

    public MCMv(VirtualReg rd, VirtualReg rs) {
        this.rd = rd;
        this.rs = rs;
    }

    @Override
    public String toString(){
        return "mv " + rd + " " + rs;
    }
}
