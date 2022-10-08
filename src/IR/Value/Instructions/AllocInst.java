package IR.Value.Instructions;

import Frontend.AST.ExpAST.ConstExpAST;
import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;


public class AllocInst extends Instruction {
    boolean isConst;
    //  AllocInst的type记录申请的type
    public AllocInst(Type type, BasicBlock bb, boolean isConst){
        super("%" + (++Value.valNumber), type, OP.Alloca, bb, true);
        this.isConst = isConst;
    }

    //  为全局变量的alloc需要知道name，我们不能用num来命名了
    public AllocInst(String name, Type type, BasicBlock basicBlock, boolean isConst){
        super(name, type, OP.Alloca, basicBlock, true);
        this.isConst = isConst;
    }

    public boolean isConst() {
        return isConst;
    }
}
