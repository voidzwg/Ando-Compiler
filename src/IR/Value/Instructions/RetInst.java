package IR.Value.Instructions;

import IR.Type.VoidType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class RetInst extends Instruction {

    public RetInst(BasicBlock block, Value value){
        super("", new VoidType(), OP.Ret, block, false);
        this.addOperand(value);
    }


    public Value getValue(){
        return this.getOperandList().get(0).getValue();
    }
}
