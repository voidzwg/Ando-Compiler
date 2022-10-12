package Pass;

import Backend.MCModule;
import IR.IRModule;
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
