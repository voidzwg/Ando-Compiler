package Pass.MC.Utils;

import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Backend.MachineValue.MachineInst.MCInst;
import Backend.Reg.Reg;
import Backend.Reg.VirtualReg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LiveAnalysis {
    private HashMap<MCBlock, BlockLiveInfo> liveAnalysisRes;
    private MCFunction mcFunction;

    public LiveAnalysis(MCFunction mcFunction){
        this.mcFunction = mcFunction;
        this.liveAnalysisRes = new HashMap<>();
        run();
    }

    private void run(){
        ArrayList<MCBlock> mcBlocks = mcFunction.getMcBlocks();
        for(MCBlock mcBlock : mcBlocks){
            //  计算use, def
            BlockLiveInfo liveInfo = new BlockLiveInfo();
            ArrayList<MCInst> mcInsts = mcBlock.getMachineInsts();
            for(MCInst mcInst : mcInsts){
                Reg defReg = mcInst.getDefReg();
                ArrayList<Reg> useReg = mcInst.getUseReg();
                if(defReg instanceof VirtualReg){
                    VirtualReg vDefReg = (VirtualReg) defReg;
                    if(!liveInfo.use.contains(vDefReg)){
                        liveInfo.def.add(vDefReg);
                    }
                }
                for(Reg reg : useReg){
                    if(reg instanceof VirtualReg){
                        VirtualReg vUseReg = (VirtualReg) reg;
                        if(!liveInfo.def.contains(vUseReg)){
                            liveInfo.use.add(vUseReg);
                        }
                    }
                }
            }
            liveInfo.in.addAll(liveInfo.use);
            liveAnalysisRes.put(mcBlock, liveInfo);
        }

        //  计算in, out
        boolean change = true;
        //  change表示在一次迭代中是否有基本块info中的in发生了改变
        while (change){
            change = false;
            for(MCBlock mb : mcBlocks){
                BlockLiveInfo info = liveAnalysisRes.get(mb);
                HashSet<VirtualReg> newIn = new HashSet<>(info.use);
                if(mb.hasSucc()){
                    info.out.addAll(liveAnalysisRes.get(mb.getTrueBlock()).in);
                    if(mb.hasFalseSucc()){
                        info.out.addAll(liveAnalysisRes.get(mb.getFalseBlock()).in);
                    }
                }
                info.out.stream()
                        .filter(out -> !info.def.contains(out))
                        .forEach(newIn::add);
                if(!newIn.equals(info.in)){
                    change = true;
                    info.in = newIn;
                }
            }

        }
    }

    public HashMap<MCBlock, BlockLiveInfo> getRes(){
        return liveAnalysisRes;
    }

    public static class BlockLiveInfo{
        private HashSet<VirtualReg> use = new HashSet<>();
        private HashSet<VirtualReg> def = new HashSet<>();
        private HashSet<VirtualReg> in = new HashSet<>();
        private HashSet<VirtualReg> out = new HashSet<>();
        public BlockLiveInfo(){
        }
    }
}
