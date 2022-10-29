package Backend.MachineValue;

import Backend.MachineValue.MachineInst.MCBinaryInst;
import Backend.MachineValue.MachineInst.MCInst;
import Backend.Reg.MCReg;
import Backend.Reg.Reg;

import java.util.ArrayList;
import java.util.HashSet;

public class MCFunction {
    private final String name;
    private final ArrayList<MCBlock> mcBlocks;
    private final ArrayList<MCBlock> mbExits;
    int stackSize;

    public MCFunction(String name, ArrayList<MCBlock> mcBlocks) {
        this.name = name;
        this.mcBlocks = mcBlocks;
        mbExits = new ArrayList<>();
        for(MCBlock mcBlock : mcBlocks){
            int len = mcBlock.getMCInsts().size();
            MCInst mcInst = mcBlock.getMCInsts().get(len - 1);
            if(mcInst.isJr() || mcInst.isSysCall()){
                mbExits.add(mcBlock);
            }
        }
    }

    public String getName() {
        return name;
    }

    public ArrayList<MCBlock> getMcBlocks() {
        return mcBlocks;
    }

    public HashSet<Reg> getVirtualReg(){
        HashSet<Reg> virtualReg = new HashSet<>();
        for(MCBlock mcBlock : mcBlocks){
            virtualReg.addAll(mcBlock.getVirtualReg());
        }
        return virtualReg;
    }

    //  setStackSize设置函数的stackSize属性
    //  并修改分配栈指针的指令
    public void setStackSize(int stackSize){
        this.stackSize = stackSize;
        MCBlock mbEntry = getMbEntry();
        ArrayList<MCBlock> mbExits = getMbExits();
        //  设置压栈的栈大小
        for(MCInst mcInst : mbEntry.getMCInsts()){
            Reg rd = mcInst.getRd();
            Reg rs1 = mcInst.getRs1();
            MCInst.Tag tag = mcInst.getTag();
            if(rd == MCReg.sp && rs1 == MCReg.sp && tag == MCInst.Tag.addiu){
                MCBinaryInst mcBinaryInst = (MCBinaryInst) mcInst;
                mcBinaryInst.setImm(-stackSize);
                break;
            }
        }
        //  设置弹栈的栈大小，注意要逆序查找，因为mbEntry和mbExit可能是一个块
        for(MCBlock mbExit : mbExits) {
            ArrayList<MCInst> mcInsts = mbExit.getMCInsts();
            int len = mcInsts.size();
            for (int i = len - 1; i >= 0; i--) {
                MCInst mcInst = mcInsts.get(i);
                Reg rd = mcInst.getRd();
                Reg rs1 = mcInst.getRs1();
                MCInst.Tag tag = mcInst.getTag();
                if (rd == MCReg.sp && rs1 == MCReg.sp && tag == MCInst.Tag.addiu) {
                    MCBinaryInst mcBinaryInst = (MCBinaryInst) mcInst;
                    mcBinaryInst.setImm(stackSize);
                    break;
                }
            }
        }
    }

    public MCBlock getMbEntry(){
        return getMcBlocks().get(0);
    }

    public ArrayList<MCBlock> getMbExits(){
        return mbExits;
    }

    public int getStackSize(){
        return stackSize;
    }
}
