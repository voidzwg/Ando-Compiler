package IR.Value.Instructions;

import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;

public class AllocInst extends Instruction {

    //  AllocInst的type记录申请的type
    public AllocInst(String name, Type type, BasicBlock bb){
        super(name, type, OP.Alloca, bb);
    }

}
