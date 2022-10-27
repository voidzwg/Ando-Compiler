package Backend.MachineValue.MachineInst;

import Backend.Reg.MCReg;

public class MCJump extends MCInst{
    private final String label;
    private final int type;
    public MCJump(String label, int type){
        this.label = label;
        this.type = type;
        if(type == 1){
            useReg.add(MCReg.ra);
        }
    }

    @Override
    public String toString(){
        String name;
        if(type == 0) name = "j";
        else if(type == 1) name = "jr";
        else name = "jal";
        return name + " " + label;
    }

    protected int getType(){
        return type;
    }
}
