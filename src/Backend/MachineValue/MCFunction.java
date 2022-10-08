package Backend.MachineValue;

import java.util.ArrayList;

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
}
