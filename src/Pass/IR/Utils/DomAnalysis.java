package Pass.IR.Utils;

import Backend.MachineValue.MachineInst.MCInst;
import IR.Value.BasicBlock;
import IR.Value.Function;
import IR.Value.Instruction;
import IR.Value.Instructions.BrInst;
import IR.Value.Instructions.RetInst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DomAnalysis {

    public static HashMap<BasicBlock, HashSet<BasicBlock>> run(Function function){
        HashMap<BasicBlock, HashSet<BasicBlock>> df = new HashMap<>();
        HashMap<BasicBlock, HashSet<BasicBlock>> dom = new HashMap<>();
        HashMap<BasicBlock, ArrayList<BasicBlock>> idoms = new HashMap<>();
        ArrayList<BasicBlock> basicBlocks = function.getBbs();

        //  初始化数据结构
        for (BasicBlock bb : basicBlocks) {
            dom.put(bb, null);
            idoms.put(bb, new ArrayList<>());
            df.put(bb, new HashSet<>());
        }

        //  先建立CFG图(Control Flow Graph)
        for(BasicBlock bb : basicBlocks){
            for(Instruction inst : bb.getInsts()){
                if(inst instanceof BrInst){
                    BrInst brInst = (BrInst) inst;
                    if(brInst.isJump()){
                        BasicBlock jumpBb = brInst.getLabelJump();
                        bb.setNxtBlock(jumpBb);
                        jumpBb.setPreBlock(bb);
                    }
                    else{
                        BasicBlock left = brInst.getLabelLeft();
                        BasicBlock right = brInst.getLabelRight();
                        bb.setNxtBlock(left);
                        bb.setNxtBlock(right);
                        left.setPreBlock(bb);
                        right.setPreBlock(bb);
                    }
                }
                else if(inst instanceof RetInst){

                }
            }
        }

        //  计算支配(dom)关系
        boolean done = false;
        while (!done){
            done = true;
            for(BasicBlock bb : basicBlocks){
                HashSet<BasicBlock> temPreBbs = null;
                for(BasicBlock preBb : bb.getPreBlocks()){
                    if(dom.get(preBb) == null){
                        continue;
                    }
                    if(temPreBbs == null){
                        temPreBbs = new HashSet<>(dom.get(preBb));
                    }
                    //  取交集
                    else{
                        HashSet<BasicBlock> mixedBbs = new HashSet<>();
                        for(BasicBlock preDomBb : dom.get(preBb)){
                            if(temPreBbs.contains(preDomBb)){
                                mixedBbs.add(preDomBb);
                            }
                        }
                        temPreBbs = mixedBbs;
                    }
                }
                if(temPreBbs == null){
                    temPreBbs = new HashSet<>();
                }

                temPreBbs.add(bb);
                if(!temPreBbs.equals(dom.get(bb))){
                    dom.replace(bb, temPreBbs);
                    done = false;
                }
            }
        }

        //  计算直接支配(idom)关系



        return df;
    }
}
