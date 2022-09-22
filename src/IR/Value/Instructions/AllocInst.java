package IR.Value.Instructions;

import Frontend.AST.ExpAST.ConstExpAST;
import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AllocInst extends Instruction {

    //  AllocInst的type记录申请的type
    public AllocInst(String name, Type type, BasicBlock bb){
        super(name, type, OP.Alloca, bb);
    }


}
