package Backend.MachineValue;

import Backend.Reg.Reg;

import java.util.ArrayList;
import java.util.HashSet;

public class MCFunction {
    private String name;
    private ArrayList<MCBlock> mcBlocks;

    public MCFunction(String name, ArrayList<MCBlock> mcBlocks) {
        this.name = name;
        this.mcBlocks = mcBlocks;
    }

    public String getName() {
        return name;
    }

    public ArrayList<MCBlock> getMcBlocks() {
        return mcBlocks;
    }

    public HashSet<Reg> getVirtualReg(){
        HashSet<Reg> virtualReg = new HashSet<>();
        for(MCBlock mcBlock : mcBlocks){
            virtualReg.addAll(mcBlock.getVirtualReg());
        }
        return virtualReg;
    }
}
