package Backend.MachineValue;

import Backend.MachineValue.MachineInst.MCInst;

import java.util.ArrayList;

public class MCBlock {
    private MCFunction parentFunc;
    private ArrayList<MCInst> machineInsts;
    private String name;
    private MCBlock trueBlock;
    private MCBlock falseBlock;
    private boolean isEntry;
    private boolean hasSucc;
    private boolean hasFalseSucc;

    public MCBlock(MCFunction parentFunc, ArrayList<MCInst> machineCodes, String name, boolean isEntry) {
        this.parentFunc = parentFunc;
        this.machineInsts = machineCodes;
        this.name = name;
        this.isEntry = isEntry;
        this.hasSucc = false;
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

    public void setTrueBlock(MCBlock trueBlock) {
        this.trueBlock = trueBlock;
        this.hasSucc = true;
    }

    public void setFalseBlock(MCBlock falseBlock) {
        this.falseBlock = falseBlock;
        this.hasSucc = true;
        this.hasFalseSucc = true;
    }

    public boolean hasSucc() {
        return hasSucc;
    }

    public boolean hasFalseSucc() {
        return hasFalseSucc;
    }

    public MCBlock getTrueBlock() {
        return trueBlock;
    }

    public MCBlock getFalseBlock() {
        return falseBlock;
    }

    public boolean isEntry() {
        return isEntry;
    }
}
