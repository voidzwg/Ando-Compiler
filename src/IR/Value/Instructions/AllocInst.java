package IR.Value.Instructions;

import Frontend.AST.ExpAST.ConstExpAST;
import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AllocInst extends Instruction {
    boolean isConst;
    //  AllocInst的type记录申请的type
    public AllocInst(String name, Type type, BasicBlock bb, boolean isConst){
        super("%" + (++Value.valNumber), type, OP.Alloca, bb);
        this.isConst = isConst;
    }


}
