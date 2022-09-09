package IR.Value;

import IR.Type.Type;
import IR.Value.Instructions.OP;

public abstract class Instruction extends User{
    BasicBlock parentbb;
    OP op;


    public Instruction(Type type, OP op, BasicBlock basicBlock) {
        super("", type);
        this.op = op;
        this.parentbb = basicBlock;
    }


    //  Getters and Setters

    public void setParentbb(BasicBlock parentbb) {
        this.parentbb = parentbb;
    }
}
