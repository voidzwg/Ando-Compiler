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
import Backend.Reg.MCReg;
import Backend.Reg.Reg;
import Backend.Reg.VirtualReg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class VirRegToMCReg {
    private MCModule mcModule;
    public VirRegToMCReg(MCModule mcModule){
        this.mcModule = mcModule;
    }

    public HashSet<Reg> isRenamed = new HashSet<>();
    public void run(){
        for(MCFunction mf : mcModule.getMcFunctions()){
            for(MCBlock mb : mf.getMcBlocks()){
                for(MCInst mcInst : mb.getMCInsts()){
                    ArrayList<Reg> allReg = new ArrayList<>();
                    allReg.addAll(mcInst.getUseReg());
                    allReg.addAll(mcInst.getDefReg());
                    for (Reg reg : allReg) {
                        if(reg instanceof VirtualReg && !isRenamed.contains(reg)) {
                            int id = ((VirtualReg) reg).getId();
                            String name;
                            if (id < 10) {
                                name = "t" + id;
                            } else name = "s" + (id - 10);
                            reg.setName(name);
                            isRenamed.add(reg);
                        }
                    }
                }
            }
        }
    }
}
