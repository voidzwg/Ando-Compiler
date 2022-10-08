package IR.Value.Instructions;

import IR.Type.IntegerType;
import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class CmpInst extends Instruction {

    public CmpInst(Value left, Value right, OP op, BasicBlock basicBlock) {
        super("%" + (++Value.valNumber), new IntegerType(1), op, basicBlock, true);
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
