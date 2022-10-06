package IR.Value.Instructions;

import IR.Type.IntegerType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class ConversionInst extends Instruction {

    public ConversionInst(OP op, Value value,BasicBlock basicBlock) {
        super("%" + (++Value.valNumber), new IntegerType(32), op, basicBlock, true);
        addOperand(value);
    }

    public Value getValue(){
        return getOperandList().get(0).getValue();
    }
}
