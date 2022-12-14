package Pass.MC;

import Backend.MCModule;
import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Backend.MachineValue.MachineInst.*;
import Backend.Reg.MCReg;
import Backend.Reg.Reg;
import Backend.Reg.VirtualReg;
import Pass.MC.Utils.LiveAnalysis;
import Pass.Pass;
import Pass.MC.Utils.LiveAnalysis.BlockLiveInfo;
import Utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class RegAllocator implements Pass.MCPass {

    private final int K = 32;
    private HashMap<MCBlock, BlockLiveInfo> liveAnalysisRes;
    //  与节点相关的数据结构
    //  initial存储所有没有被precolor且没被算法处理过的寄存器
    private HashSet<Reg> initial;
    //  simplifyWorkList存储所有需要simplify的寄存器
    private HashSet<Reg> simplifyWorkList;
    //  spillWorkList存储所有需要spill的寄存器
    private HashSet<Reg> spillWorkList;
    //  freezeWorkList存储所有需要freeze的寄存器
    private HashSet<Reg> freezeWorkList;
    //  coalesced存储所有被合并的寄存器
    private HashSet<Reg> coalescedNodes;
    //  spilled存储所有被spill的寄存器
    private HashSet<Reg> spilledNodes;
    //  colored存储所有被分配颜色的寄存器
    private HashSet<Reg> coloredNodes;
    //  selectStack存储所有被simplify的寄存器
    private Stack<Reg> selectStack;

    //  下面是move相关的数据结构
    //  coalescedMoves存储所有被合并的move
    private HashSet<MCMV> coalescedMoves;
    //  constrainedMoves存储所有被constrained的move
    private HashSet<MCMV> constrainedMoves;
    //  frozenMoves存储所有被freeze的move
    private HashSet<MCMV> frozenMoves;
    //  workList存放所有的move指令
    private HashSet<MCMV> workListMoves;
    //  activeList存放所有活跃的move指令
    private HashSet<MCMV> activeMoves;

    //  下面是其他相关的数据结构
    //  adjSet存储边，即两个寄存器之间的冲突
    private HashSet<Pair<Reg, Reg>> adjSet;
    //  adjList为邻接表，为每个点存储与其相连的点
    private HashMap<Reg, HashSet<Reg>> adjList;
    //  degree存储每个点的度数
    private HashMap<Reg, Integer> degree;
    //  moveList用于映射寄存器与其对应的move指令
    private HashMap<Reg, HashSet<MCMV>> moveList;
    private HashMap<Reg, Reg> alias;
    // loopDepth用于记录Reg所在的深度，作为selectSpill的参考因素
    private HashMap<Reg, Integer> loopDepth;
    //  rewriteMap用于保存rewrite函数中为溢出的虚拟寄存器暂时分配的寄存器
    private HashMap<Reg, VirtualReg> rewriteRegMap;
    //  rewritePosMap记录溢出的Reg在栈上的位置
    private HashMap<Reg, Integer> rewritePosMap;

    //  ando架构中这些寄存器遵守abi约定，其他全部用于分配
    private boolean idPreColored(int id){
        return (id >= 0 && id <= 2) || (id >= 4 && id <= 7) || id == 26 || id == 27 || id == 29 || id == 31;
    }

    @Override
    public void run(MCModule mcModule) {
        ArrayList<MCFunction> mcFunctions = mcModule.getMcFunctions();
        //  对每个函数进行寄存器分配
        for(MCFunction mf : mcFunctions){
            boolean done = false;
            while (!done) {
                init(mf);
                //  先做活跃变量分析
                LiveAnalysis liveAnalysis = new LiveAnalysis(mf);
                liveAnalysisRes = liveAnalysis.getRes();
                //  建立相交图
                build(mf);
                //  初始化workList
                mkWorkList();

                do {
                    if (!simplifyWorkList.isEmpty()) {
                        simplify();
                    } else if (!workListMoves.isEmpty()) {
                        coalesce();
                    } else if (!freezeWorkList.isEmpty()) {
                        freeze();
                    } else if (!spillWorkList.isEmpty()) {
                        selectSpill();
                    }
                } while (!simplifyWorkList.isEmpty() || !workListMoves.isEmpty() || !freezeWorkList.isEmpty() || !spillWorkList.isEmpty());

                assignColors(mf);
                if (!spilledNodes.isEmpty()) {
                    rewriteProgram(mf);
                } else {
                    done = true;
                }
            }
        }
    }

    private void rewriteProgram(MCFunction mf){
        int size = mf.getStackSize();
        for(Reg r : spilledNodes){
            for(MCBlock mb : mf.getMcBlocks()) {
                //  我们从mf的stackSize之上为新变量分配空间
                ArrayList<MCInst> mcInsts = mb.getMCInsts();
                for (int i = 0; i < mcInsts.size(); i++) {
                    MCInst inst = mcInsts.get(i);
                    if(inst.getDefReg().contains(r)){
                        VirtualReg newReg = new VirtualReg();
                        rewriteRegMap.put(r, newReg);
                        MCInst swInst = new MCSW(newReg, MCReg.sp, size);
                        rewritePosMap.put(r, size);
                        size += 4;
                        mb.insertInst(swInst, i + 1);
                        inst.replaceReg(r, newReg);
                    }
                    if(inst.getUseReg().contains(r)){
                        VirtualReg reg = rewriteRegMap.get(r);
                        int pos = rewritePosMap.get(r);
                        MCInst lwInst = new MCLW(reg, MCReg.sp, pos);
                        mb.insertInst(lwInst, i);
                        inst.replaceReg(r, reg);
                    }
                }
            }
        }
        //  更新stackSize
        mf.setStackSize(size);
    }

    private void assignColors(MCFunction mf){
        HashMap<Reg, MCReg> colored = new HashMap<>();

        while (!selectStack.empty()){
            Reg n = selectStack.pop();
            HashSet<Integer> okColors = new HashSet<>();
            for(int i = 0; i < K; i++){
                if(!idPreColored(i)) {
                    okColors.add(i);
                }
            }
            for(Reg w : adjList.getOrDefault(n, new HashSet<>())){
                Reg u = getAlias(w);
                if(coloredNodes.contains(u)){
                    okColors.remove(colored.get(u).getId());
                }
            }
            if(okColors.isEmpty()){
                spilledNodes.add(n);
            }else{
                coloredNodes.add(n);
                int pid = okColors.iterator().next();
                colored.put(n, new MCReg(pid, true));
            }
        }

        //  如果发现有真spill, 那就直接return重开
        //  都出现spill了还染个勾巴色
        if (!spillWorkList.isEmpty()){
            return ;
        }

        for(Reg n : coalescedNodes){
            Reg tmp = getAlias(n);
            if(tmp.isPrecolored()){
                colored.put(n, (MCReg) tmp);
            }
            else colored.put(n, colored.get(tmp));
        }

        for (MCBlock mb : mf.getMcBlocks()) {
            //  deleteInsts删除冗余的move指令
            ArrayList<MCInst> deleteInsts = new ArrayList<>();
            for (MCInst mcInst : mb.getMCInsts()) {
                ArrayList<Reg> defs = new ArrayList<>(mcInst.getDefReg());
                ArrayList<Reg> uses = new ArrayList<>(mcInst.getUseReg());

                defs.stream().filter(colored::containsKey)
                        .forEach(def -> mcInst.replaceReg(def, colored.get(def)));
                uses.stream().filter(colored::containsKey)
                        .forEach(use -> mcInst.replaceReg(use, colored.get(use)));

                if(mcInst instanceof MCMV){
                    MCMV mcmv = (MCMV) mcInst;
                    Reg rd = mcmv.getDst();
                    Reg rs = mcmv.getSrc();
                    if(rd == rs){
                        deleteInsts.add(mcInst);
                    }
                }
            }

            for(MCInst deleteInst : deleteInsts){
                mb.deleteInst(deleteInst);
            }
        }

    }

    private void selectSpill(){
        Reg m = selectSpillNode();
        spillWorkList.remove(m);
        simplifyWorkList.add(m);
        freezeMoves(m);
    }

    //  可优化--优先选择度数大，循环深度浅，生命周期长的节点
    //  V1.0 先只考虑度数大
    private Reg selectSpillNode(){
        double maxScore = 0.0;
        Reg ans = null;
        for(Reg reg : spillWorkList){
            double curScore = 0.0;
            curScore = degree.get(reg);
            if(curScore >= maxScore){
                maxScore = curScore;
                ans = reg;
            }
        }
        return ans;
    }

    private void freeze(){
        Reg u = freezeWorkList.iterator().next();
        freezeWorkList.remove(u);
        simplifyWorkList.add(u);
        freezeMoves(u);
    }

    private void freezeMoves(Reg u){
        for (MCMV m : getNodeMoves(u)) {
            if (activeMoves.contains(m)) {
                activeMoves.remove(m);
            } else {
                workListMoves.remove(m);
            }
            frozenMoves.add(m);
            Reg v = m.getDst().equals(u) ? m.getSrc() : m.getDst();
            if (getNodeMoves(v).isEmpty() && getDegree(v) < K) {
                freezeWorkList.remove(v);
                simplifyWorkList.add(v);
            }
        }
    }

    private void coalesce(){
        MCMV mcmv = workListMoves.iterator().next();
        Reg x = getAlias(mcmv.getDst());
        Reg y = getAlias(mcmv.getSrc());
        Reg u, v;
        //  如果有precolored, 那么令u为precolored, v为另一个
        if(y.isPrecolored()){
            u = y;
            v = x;
        }
        else{
            u = x;
            v = y;
        }
        workListMoves.remove(mcmv);
        if (u.equals(v)) {
            coalescedMoves.add(mcmv);
            addWorkList(u);
        } else if (v.isPrecolored() || adjSet.contains(new Pair<>(u, v))) {
            constrainedMoves.add(mcmv);
            addWorkList(u);
            addWorkList(v);
        } else if ((u.isPrecolored() && adjOk(u, v)) ||
                (!u.isPrecolored() && conservative(getAdjacent(u), getAdjacent(v)))) {
            coalescedMoves.add(mcmv);
            combine(u, v);
            addWorkList(u);
        } else {
            activeMoves.add(mcmv);
        }
    }

    private void combine(Reg u, Reg v){
        if (freezeWorkList.contains(v)) {
            freezeWorkList.remove(v);
        } else {
            spillWorkList.remove(v);
        }

        coalescedNodes.add(v);
        alias.put(v, u);
        moveList.get(u).addAll(moveList.get(v));

        for(Reg adjReg : getAdjacent(v)){
            addEdge(adjReg, u);
            decrementDegree(adjReg);
        }

        if (getDegree(u) >= K && freezeWorkList.contains(u)) {
            freezeWorkList.remove(u);
            spillWorkList.add(u);
        }
    }

    private boolean adjOk(Reg u, Reg v) {
        return getAdjacent(v).stream().allMatch(t -> ok(t, u));
    }

    private boolean ok(Reg t, Reg r) {
        return getDegree(t) < K || t.isPrecolored() || adjSet.contains(new Pair<>(t, r));
    }

    private boolean conservative(HashSet<Reg> u, HashSet<Reg> v) {
        HashSet<Reg> nodes = new HashSet<>(u);
        nodes.addAll(v);
        int cnt = (int) nodes.stream().filter(n -> getDegree(n) >= K).count();
        return cnt < K;
    }


    private void addWorkList(Reg u){
        if (!u.isPrecolored() && !isMoveRelated(u) && getDegree(u) < K) {
            freezeWorkList.remove(u);
            simplifyWorkList.add(u);
        }
    }

    private Reg getAlias(Reg reg){
        if(coalescedNodes.contains(reg)){
            return getAlias(alias.get(reg));
        }
        return reg;
    }

    //  从simpleWorkList中取出一个点，将其加入selectStack中，并将其相邻点的度数减一
    private void simplify(){
        Reg reg = simplifyWorkList.iterator().next();
        simplifyWorkList.remove(reg);
        selectStack.push(reg);
        for(Reg adj : getAdjacent(reg)){
            decrementDegree(adj);
        }
    }

    private void decrementDegree(Reg reg){
        int deg = getDegree(reg);
        degree.replace(reg, deg - 1);
        if(deg == K){
            Set<Reg> enableList = new HashSet<>();
            enableList.add(reg);
            enableList.addAll(getAdjacent(reg));
            enableMoves(enableList);
            spillWorkList.remove(reg);
            if(reg.isPrecolored()){
                freezeWorkList.add(reg);
            }else{
                simplifyWorkList.add(reg);
            }
        }
    }

    private void enableMoves(Set<Reg> regs){
        for(Reg reg : regs){
            for(MCMV mv : getNodeMoves(reg)){
                if(activeMoves.contains(mv)){
                    activeMoves.remove(mv);
                    workListMoves.add(mv);
                }
            }
        }
    }

    private Set<MCMV> getNodeMoves(Reg reg){
        return moveList
                .getOrDefault(reg, new HashSet<>())
                .stream()
                .filter(mv -> activeMoves.contains(mv) || workListMoves.contains(mv))
                .collect(Collectors.toSet());
    }

    private HashSet<Reg> getAdjacent(Reg n) {
        HashSet<Reg> res = new HashSet<>(adjList.getOrDefault(n, new HashSet<>()));
        selectStack.forEach(res::remove);
        coalescedNodes.forEach(res::remove);
        return res;
    }

    private void mkWorkList() {
        for (Reg reg : initial) {
            if (getDegree(reg) >= K) {
                spillWorkList.add(reg);
            } else if (isMoveRelated(reg)) {
                freezeWorkList.add(reg);
            } else {
                simplifyWorkList.add(reg);
            }
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
                for (Reg d : defReg) {
                    for (Reg l : live) {
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

    private boolean isMoveRelated(Reg u) {
        return moveList.containsKey(u);
    }

    private void init(MCFunction mf){
        liveAnalysisRes = new HashMap<>();
        moveList = new HashMap<>();
        adjSet = new HashSet<>();
        adjList = new HashMap<>();
        degree = new HashMap<>();
        initial = mf.getVirtualReg();
        workListMoves = new HashSet<>();
        spillWorkList = new HashSet<>();
        freezeWorkList = new HashSet<>();
        simplifyWorkList = new HashSet<>();
        coalescedNodes = new HashSet<>();
        spilledNodes = new HashSet<>();
        coloredNodes = new HashSet<>();
        selectStack = new Stack<>();
        coalescedMoves = new HashSet<>();
        constrainedMoves = new HashSet<>();
        frozenMoves = new HashSet<>();
        activeMoves = new HashSet<>();
        alias = new HashMap<>();
        loopDepth = new HashMap<>();
        rewritePosMap = new HashMap<>();
        rewriteRegMap = new HashMap<>();
        loopDepth = new HashMap<>();
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
