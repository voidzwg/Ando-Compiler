package Backend.MachineValue.MachineInst;

import Backend.Reg.MCReg;

public class MCReturn extends MCInst {

    public MCReturn(){
        useReg.add(new MCReg("ra"));
    }
    @Override
    public String toString(){
        return "jr $ra";
    }
}
