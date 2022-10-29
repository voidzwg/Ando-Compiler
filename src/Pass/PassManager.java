package Pass;

import Backend.MCModule;
import IR.IRModule;
import Pass.IR.Mem2Reg;
import Pass.MC.RegAllocator;
import Pass.Pass.*;

import java.util.ArrayList;

public class PassManager {
    private static PassManager passManager = new PassManager();
    public static PassManager getInstance(){
        return passManager;
    }
    ArrayList<IRPass> irPasses = new ArrayList<>();
    ArrayList<MCPass> mcPasses = new ArrayList<>();

    private PassManager(){
        //  IRPasses
        irPasses.add(new Mem2Reg());

        //  MCPasses
        mcPasses.add(new RegAllocator());
    }

    public void runIRPasses(IRModule irModule){
        irPasses.forEach(irPass -> {
            irPass.run(irModule);
        });
    }

    public void runMCPasses(MCModule mcModule){
        mcPasses.forEach(mcPass -> {
            mcPass.run(mcModule);
        });
    }
}
