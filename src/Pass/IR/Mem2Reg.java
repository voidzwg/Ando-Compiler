package Pass.IR;

import IR.IRModule;
import IR.Value.BasicBlock;
import IR.Value.Function;
import Pass.IR.Utils.DomAnalysis;
import Pass.Pass;

import java.util.HashMap;
import java.util.HashSet;

public class Mem2Reg implements Pass.IRPass {

    @Override
    public String getName() {
        return "mem2reg";
    }

    @Override
    public void run(IRModule module) {
        for(Function function : module.getFunctions()){
            HashMap<BasicBlock, HashSet<BasicBlock>> df = DomAnalysis.run(function);

        }
    }
}
