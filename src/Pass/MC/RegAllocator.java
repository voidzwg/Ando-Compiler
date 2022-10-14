package Pass.MC;

import Backend.MCModule;
import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Backend.MachineValue.MachineInst.MCInst;
import Backend.MachineValue.MachineInst.MCMV;
import Backend.Reg.Reg;
import Pass.MC.Utils.LiveAnalysis;
import Pass.Pass;
import Pass.MC.Utils.LiveAnalysis.BlockLiveInfo;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RegAllocator implements Pass.MCPass {

    private HashMap<MCBlock, BlockLiveInfo> liveAnalysisRes;
    //  moveList用于映射寄存器与其对应的move指令
    private HashMap<Reg, HashSet<MCMV>> moveList;
    //  workList存放所有的move指令
    private HashSet<MCMV> workListMoves;
    //  adjSet存储边，即两个寄存器之间的冲突
    private HashSet<Pair<Reg, Reg>> adjSet;
    //  adjList为邻接表，为每个点存储与其相连的点
    private HashMap<Reg, HashSet<Reg>> adjList;
    //  degree存储每个点的度数
    private HashMap<Reg, Integer> degree;

    @Override
    public void run(MCModule mcModule) {
        ArrayList<MCFunction> mcFunctions = mcModule.getMcFunctions();
        //  对每个函数进行寄存器分配
        for(MCFunction mf : mcFunctions){
            init();
            //  先做活跃变量分析
            LiveAnalysis liveAnalysis = new LiveAnalysis(mf);
            liveAnalysisRes = liveAnalysis.getRes();
            //  建立相交图
            build(mf);

        }
    }

    private void build(MCFunction mf){
        ArrayList<MCBlock> mcBlocks = mf.getMcBlocks();
        for(MCBlock mb : mcBlocks){
            BlockLiveInfo liveInfo = liveAnalysisRes.get(mb);
            HashSet<Reg> live = liveInfo.out;
            ArrayList<MCInst> mcInsts = mb.getMCInsts();
            for(int i = mcInsts.size() - 1; i >= 0; i--){
                MCInst mcInst = mcInsts.get(i);
                ArrayList<Reg> useReg = mcInst.getUseReg();
                ArrayList<Reg> defReg = mcInst.getDefReg();
                if(mcInst instanceof MCMV){
                    Reg use = useReg.get(0);
                    Reg def = defReg.get(0);
                    live.remove(use);

                    mapAddElement(moveList, use, (MCMV) mcInst);
                    mapAddElement(moveList, def, (MCMV) mcInst);

                    workListMoves.add((MCMV) mcInst);
                }
                //  更新out集合
                live.addAll(defReg);
                for (var d : defReg) {
                    for (var l : live) {
                        addEdge(l, d);
                    }
                    // live := use(I) ∪ (live\def(I))
                    live.remove(d);
                }
                live.addAll(useReg);
            }

        }

    }

    private void addEdge(Reg u, Reg v){
        Pair<Reg, Reg> edge = new Pair<>(u, v);

        if (!adjSet.contains(edge) && !u.equals(v)) {
            adjSet.add(edge);
            adjSet.add(new Pair<>(v, u));
            if (!u.isPrecolored()) {
                mapAddElement(adjList, u, v);
                degree.put(u, getDegree(u) + 1);
            }
            if (!v.isPrecolored()) {
                mapAddElement(adjList, v, u);
                degree.put(v, getDegree(v) + 1);
            }
        }
    }

    private int getDegree(Reg u) {
        return degree.getOrDefault(u, 0);
    }


    private void init(){
        liveAnalysisRes = new HashMap<>();
        moveList = new HashMap<>();
        adjSet = new HashSet<>();
        adjList = new HashMap<>();
        degree = new HashMap<>();
    }

    private void mapAddElement(HashMap<Reg, HashSet<MCMV>> map, Reg u, MCMV v) {
        map.putIfAbsent(u, new HashSet<>());
        map.get(u).add(v);
    }

    private void mapAddElement(HashMap<Reg, HashSet<Reg>> map, Reg u, Reg v) {
        map.putIfAbsent(u, new HashSet<>());
        map.get(u).add(v);
    }

    @Override
    public String getName() {
        return "RegAlloc";
    }
}
