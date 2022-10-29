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
        //  dom记录支配每个块被哪些块支配
        HashMap<BasicBlock, HashSet<BasicBlock>> dom = new HashMap<>();
        //  idom记录每个Bb的直接支配者
        HashMap<BasicBlock, BasicBlock> idom = new HashMap<>();
        //  idoms记录每个Bb直接支配哪些块
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
        for(BasicBlock bb : basicBlocks){
            HashSet<BasicBlock> domSet = dom.get(bb);
            if(domSet.size() == 1){
                idom.put(bb, null);
            }
            for(BasicBlock domBb : domSet){
                if(domBb.equals(bb)){
                    continue;
                }

                boolean isIdom = true;
                for (BasicBlock tmpDomBb : domSet) {
                    if (!tmpDomBb.equals(bb) &&
                            !tmpDomBb.equals(domBb) &&
                            dom.get(tmpDomBb).contains(domBb)) {
                        isIdom = false;
                        break;
                    }
                }

                if (isIdom) {
                    idom.put(bb, domBb);
                    idoms.get(domBb).add(bb);
                    break;
                }
            }
        }
        function.setIdoms(idoms);

        //  建立支配树
        BasicBlock bbEntry = function.getBbEntry();
        buildDomTree(bbEntry, 0, function);

        //  计算DF
        for (BasicBlock bb : basicBlocks) {
            if (bb.getPreBlocks().size() > 1) {

                for (BasicBlock p : bb.getPreBlocks()) {
                    BasicBlock runner = p;
                    while (!runner.equals(idom.get(bb))
                            && df.containsKey(runner)) {
                        df.get(runner).add(bb);
                        runner = idom.get(runner);
                    }
                }
            }
        }

        return df;
    }

    private static void buildDomTree(BasicBlock bb, int domLV, Function function){
        bb.setDomLV(domLV);
        for(BasicBlock sonBb : function.getIdoms().get(bb)){
            buildDomTree(sonBb, domLV + 1, function);
        }
    }
}
