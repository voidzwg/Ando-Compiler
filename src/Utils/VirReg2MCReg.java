package Utils;

//  这个类用于我还没写完寄存器分配的时候
//  用这个类将我用到的虚拟寄存器转换为物理寄存器以便mars执行
//  当然，使用的寄存器数量有要求，不能超过18个
//  因为这个类将把$0-$17映射到$t0-$t9和$s0-$s7
//  多了的话肯定不行，不过感觉测试还是够用的(指手写样例

import Backend.MCModule;
import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Backend.MachineValue.MachineInst.MCInst;
import Backend.MachineValue.MachineInst.MCLW;
import Backend.MachineValue.MachineInst.MCSW;
import Backend.Reg.MCReg;
import Backend.Reg.Reg;
import Backend.Reg.VirtualReg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

//  为什么这个类叫VirReg2MCReg, 而不叫寄存器分配呢
//  因为ando觉得这个策略太笨蛋了以至于不配叫做寄存器分配
//  可是为了方便debug 笨蛋ando还是要写一个笨蛋策略qwq
public class VirReg2MCReg {
    private MCModule mcModule;
    public VirReg2MCReg(MCModule mcModule){
        this.mcModule = mcModule;
    }

    private boolean idPreColored(int id){
        return (id >= 0 && id <= 2) || (id >= 4 && id <= 7) || id == 26 || id == 27 || id == 29 || id == 31;
    }
    private HashSet<Integer> freeIds;
    private HashSet<Reg> spilledReg;
    private HashMap<Reg, Integer> colored;

    //  记录溢出的寄存器在栈上的位置
    private HashMap<Reg, Integer> spPos;

    private void init(){
        freeIds = new HashSet<>();
        for(int i = 0; i < 32; i++){
            if(!idPreColored(i)){
                freeIds.add(i);
            }
        }
        colored = new HashMap<>();
        spPos = new HashMap<>();
        spilledReg = new HashSet<>();
    }

    public void run(){
        for(MCFunction mf : mcModule.getMcFunctions()){
            init();
            //  先把能分配的都分配了
            boolean isFull = false;
            for(MCBlock mb : mf.getMcBlocks()){
                for(MCInst mcInst : mb.getMCInsts()){
                    ArrayList<Reg> allReg = new ArrayList<>();
                    allReg.addAll(mcInst.getUseReg());
                    allReg.addAll(mcInst.getDefReg());
                    for (Reg reg : allReg) {
                        if(reg instanceof VirtualReg) {
                            if(!isFull) {
                                Reg allocReg;
                                int id;
                                if (!colored.containsKey(reg)) {
                                    id = freeIds.iterator().next();
                                    colored.put(reg, id);
                                    freeIds.remove(id);
                                } else {
                                    id = colored.get(reg);
                                }
                                allocReg = new MCReg(id, true);
                                mcInst.replaceReg(reg, allocReg);
                                if (freeIds.size() == 2) {
                                    isFull = true;
                                    break;
                                }
                            }
                            else{
                                if(colored.containsKey(reg)){
                                    int id = colored.get(reg);
                                    Reg allocReg = new MCReg(id, true);
                                    mcInst.replaceReg(reg, allocReg);
                                }
                                else spilledReg.add(reg);
                            }
                        }
                    }
                }
            }

            if(!isFull) continue;

            //  目前所有的寄存器都分完了，只剩下两个用于临时存取内存的寄存器
            int tmpId = freeIds.iterator().next();
            freeIds.remove(tmpId);
            int tmpId2 = freeIds.iterator().next();

            Reg tmpReg = new MCReg(tmpId, true);
            Reg tmpReg2 = new MCReg(tmpId2, true);
            int size = mf.getStackSize();
            for(MCBlock mb : mf.getMcBlocks()){
                ArrayList<MCInst> mcInsts = mb.getMCInsts();
                for(int i = 0; i < mcInsts.size(); i++){
                    MCInst mcInst = mcInsts.get(i);

                    ArrayList<Reg> defReg = mcInst.getDefReg();
                    ArrayList<Reg> useReg = mcInst.getUseReg();

                    int cnt = 0;
                    ArrayList<Reg> oldReg = new ArrayList<>();
                    ArrayList<Reg> newReg = new ArrayList<>();
                    for(Reg reg : useReg){
                        if(spilledReg.contains(reg)){
                            cnt++;
                            Reg tmp;
                            if(cnt == 1) tmp = tmpReg;
                            else tmp = tmpReg2;

                            //  以防改变useReg，我们先把要替换的放到数组里，一会统一替换
                            oldReg.add(reg);
                            newReg.add(tmp);

                            //  插入lw指令
                            int pos = spPos.get(reg);
                            MCInst lwInst = new MCLW(tmp, MCReg.sp, pos);
                            mb.insertInst(lwInst, i);
                            i++;
                        }
                    }
                    //  开始替换
                    for(int j = 0; j < oldReg.size(); j++){
                        Reg old = oldReg.get(j);
                        Reg tmp = newReg.get(j);
                        mcInst.replaceReg(old, tmp);
                    }

                    if(defReg.size() > 0 && spilledReg.contains(defReg.get(0))){
                        Reg reg = defReg.get(0);
                        mcInst.replaceReg(reg, tmpReg);
                        //  插入sw指令
                        MCInst swInst = new MCSW(tmpReg, MCReg.sp, size);
                        mb.insertInst(swInst, i + 1);
                        spPos.put(reg, size);
                        size += 4;
                        i++;
                    }
                }
            }
            mf.setStackSize(size);
        }
    }
}
