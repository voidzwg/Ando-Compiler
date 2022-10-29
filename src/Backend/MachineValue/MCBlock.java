package Backend.MachineValue;

import Backend.MachineValue.MachineInst.MCInst;
import Backend.Reg.Reg;
import Backend.Reg.VirtualReg;

import java.util.ArrayList;
import java.util.HashSet;

public class MCBlock {
    private final MCFunction parentFunc;
    private final ArrayList<MCInst> machineInsts;
    private final String name;
    private MCBlock trueBlock;
    private MCBlock falseBlock;
    private final boolean isEntry;
    private boolean hasSucc;
    private boolean hasFalseSucc;
    private int loopDepth;

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

    public ArrayList<MCInst> getMCInsts() {
        return machineInsts;
    }

    public void setLoopDepth(int x){
        this.loopDepth = x;
    }
    public int getLoopDepth() {
        return loopDepth;
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

    public HashSet<Reg> getVirtualReg(){
        HashSet<Reg> virtualReg = new HashSet<>();
        for(MCInst mcInst : machineInsts){
            mcInst.getUseReg().stream().filter(reg -> reg instanceof VirtualReg).forEach(virtualReg::add);
            mcInst.getDefReg().stream().filter(reg -> reg instanceof VirtualReg).forEach(virtualReg::add);
        }
        return virtualReg;
    }

    //  在pos位置插入指令mcInst
    public void insertInst(MCInst mcInst, int pos){
        machineInsts.add(pos, mcInst);
    }

    public void deleteInst(MCInst mcInst){
        machineInsts.remove(mcInst);
    }
}
