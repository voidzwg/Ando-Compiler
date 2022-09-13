package IR.Value.Instructions;

import IR.Type.VoidType;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

public class BrInst extends Instruction {

    public BrInst(Value judVal, BasicBlock left, BasicBlock right, BasicBlock basicBlock) {
        super("", new VoidType(), OP.Br, basicBlock);
        this.addOperand(judVal);
        this.addOperand(left);
        this.addOperand(right);
    }

    public Value getJudVal(){
        return this.getOperandList().get(0).getValue();
    }

    public BasicBlock getLabelLeft(){
        return (BasicBlock) this.getOperandList().get(1).getValue();
    }

    public BasicBlock getLabelRight(){
        return (BasicBlock) this.getOperandList().get(2).getValue();
    }
}
