package Backend.Reg;

import Backend.MachineValue.MachineInst.MCReturn;

public class Reg {
    private String name;
    public Reg(String name){
        this.name = name;
    }
    public boolean isPrecolored() {
        return this instanceof MCReg && !((MCReg) this).isAllocated;
    }

    @Override
    public String toString(){
        return "$" + name;
    }

}
