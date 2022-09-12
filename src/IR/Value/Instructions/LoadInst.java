package IR.Value.Instructions;

import IR.Type.IntegerType;
import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class LoadInst extends Instruction {

    public LoadInst(Value pointer, BasicBlock basicBlock) {
        super("%" + (++Value.valNumber), new IntegerType(32), OP.Load, basicBlock);
        this.addOperand(pointer);
    }

    public Value getPointer(){
        return this.getOperandList().get(0).getValue();
    }
}
