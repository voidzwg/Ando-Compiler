package Pass;

import Backend.MCModule;
import IR.IRModule;
import Pass.MC.RegAllocator;

import java.util.ArrayList;

public class PassManager {
    private static PassManager passManager = new PassManager();
    public static PassManager getInstance(){
        return passManager;
    }
    ArrayList<Pass.Pass.IRPass> irPasses = new ArrayList<>();
    ArrayList<Pass.Pass.MCPass> mcPasses = new ArrayList<>();

    private PassManager(){
        mcPasses.add(new RegAllocator());
    }

    public void runIRPasses(IRModule irModule){
        for(Pass.Pass.IRPass irPass : irPasses){
            irPass.run(irModule);
        }
    }

    public void runMCPasses(MCModule mcModule){
        for(Pass.Pass.MCPass mcPass : mcPasses){
            mcPass.run(mcModule);
        }
    }
}
