package IR.Value;

import IR.Type.Type;
import IR.Value.Instructions.OP;

public abstract class Instruction extends User{
    BasicBlock parentbb;
    OP op;


    public Instruction(String name, Type type, OP op, BasicBlock basicBlock) {
        super(name, type);
        this.op = op;
        this.parentbb = basicBlock;
    }


    //  Getters and Setters

    public OP getOp() {
        return op;
    }
}
