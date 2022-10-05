package Backend.MachineValue;

import Backend.MachineValue.MachineInst.MCInst;

import java.util.ArrayList;

public class MCBlock {
    private MCFunction parentFunc;
    private ArrayList<MCInst> machineInsts;
    private String name;

    public MCBlock(MCFunction parentFunc, ArrayList<MCInst> machineCodes, String name) {
        this.parentFunc = parentFunc;
        this.machineInsts = machineCodes;
        this.name = name;
    }

    public void addInst(MCInst mcInst){
        machineInsts.add(mcInst);
    }

    public ArrayList<MCInst> getMachineInsts() {
        return machineInsts;
    }

    public String getName() {
        return name;
    }
}
