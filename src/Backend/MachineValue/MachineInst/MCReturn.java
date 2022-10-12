package Backend.MachineValue.MachineInst;

import Backend.Reg.PhysicalReg;
import Backend.Reg.Reg;

public class MCReturn extends MCInst {

    public MCReturn(){
        useReg.add(new PhysicalReg("ra"));
    }
    @Override
    public String toString(){
        return "jr $ra";
    }
}
