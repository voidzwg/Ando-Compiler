package IR.Value;

import IR.Type.Type;
import IR.Value.Instructions.OP;

public abstract class Instruction extends Value{
    BasicBlock parentbb;
    OP op;


    public Instruction(String name, Type type) {
        super(name, type);
    }


    //  Getters and Setters

    public void setParentbb(BasicBlock parentbb) {
        this.parentbb = parentbb;
    }
}
