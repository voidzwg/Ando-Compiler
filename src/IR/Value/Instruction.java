package IR.Value;

import IR.Type.Type;
import IR.Value.Instructions.OP;

public abstract class Instruction extends User{
    BasicBlock parentbb;
    OP op;
    //  这里的hasName指的是是否有类似%3，%4的name，为了便于后续命名工作
    //  因此一些特殊的名字我们是不算的
    boolean hasName;


    public Instruction(String name, Type type, OP op, BasicBlock basicBlock, boolean hasName) {
        super(name, type);
        this.op = op;
        this.parentbb = basicBlock;
        this.hasName = hasName;
    }


    //  Getters and Setters

    public OP getOp() {
        return op;
    }

    public void setHasName(boolean hasName) {
        this.hasName = hasName;
    }

    public boolean hasName(){
        return hasName;
    }
}
