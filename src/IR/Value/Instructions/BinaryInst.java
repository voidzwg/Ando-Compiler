package IR.Value.Instructions;

import IR.Type.IntegerType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class BinaryInst extends Instruction {

    public BinaryInst(OP op, Value left, Value right,BasicBlock basicBlock) {
        super("%" + (++Value.valNumber), new IntegerType(32), op, basicBlock, true);
        this.addOperand(left);
        this.addOperand(right);
    }

    public Value getLeftVal(){
        return getOperandList().get(0).getValue();
    }

    public Value getRightVal(){
        return getOperandList().get(1).getValue();
    }
}
