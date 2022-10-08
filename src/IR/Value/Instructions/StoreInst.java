package IR.Value.Instructions;

import IR.Type.IntegerType;
import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class StoreInst extends Instruction {

    public StoreInst(BasicBlock basicBlock, Value value, Value pointer) {
        super("", new IntegerType(32), OP.Store, basicBlock, false);
        this.addOperand(value);
        this.addOperand(pointer);
    }

    public Value getValue(){
        return this.getOperandList().get(0).getValue();
    }

    public Value getPointer(){
        return this.getOperandList().get(1).getValue();
    }
}
