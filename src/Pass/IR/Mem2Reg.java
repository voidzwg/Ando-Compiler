package Pass.IR;

import IR.IRModule;
import IR.Value.BasicBlock;
import IR.Value.Function;
import IR.Value.Instruction;
import IR.Value.Instructions.AllocInst;
import IR.Value.Instructions.StoreInst;
import IR.Value.Value;
import Pass.IR.Utils.DomAnalysis;
import Pass.Pass;

import java.util.ArrayList;
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
            ArrayList<BasicBlock> basicBlocks = function.getBbs();
            HashMap<BasicBlock, HashSet<BasicBlock>> df = DomAnalysis.run(function);
            HashMap<AllocInst, ArrayList<BasicBlock>> defMap = new HashMap<>();
            ArrayList<AllocInst> defs = new ArrayList<>();

            //  初始化alloca
            for(BasicBlock bb : basicBlocks){
                for(Instruction inst : bb.getInsts()){
                    if(inst instanceof AllocInst){
                        AllocInst allocInst = (AllocInst) inst;
                        if(allocInst.getAllocType().isIntegerTy()) {
                            defMap.put(allocInst, new ArrayList<>());
                            defs.add(allocInst);
                        }
                    }
                }
            }

            //  初始化store
            for(BasicBlock bb : basicBlocks){
                for(Instruction inst : bb.getInsts()){
                    if(inst instanceof StoreInst){
                        StoreInst storeInst = (StoreInst) inst;
                        Value pointer = storeInst.getPointer();
                        if(pointer instanceof AllocInst && defMap.containsKey(pointer)) {
                            defMap.get(pointer).add(bb);
                        }
                    }
                }
            }

            //  删除无用的alloca
            HashMap<AllocInst, ArrayList<BasicBlock>> tmpDefMap = new HashMap<>(defMap);
            for (AllocInst allocaInst : defMap.keySet()) {
                if (defMap.get(allocaInst).size() == 0) {
                    tmpDefMap.remove(allocaInst);
                    defs.remove(allocaInst);
                }
            }
            defMap = tmpDefMap;


        }
    }
}
