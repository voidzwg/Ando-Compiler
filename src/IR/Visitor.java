package IR;

import Frontend.AST.BlockAST;
import Frontend.AST.CompUnitAST;
import Frontend.AST.FuncDefAST;
import Frontend.AST.StmtAST;
import IR.Type.IntegerType;
import IR.Type.VoidType;
import IR.Value.*;
import IR.Value.Instructions.RetInst;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Visitor {
    //
    private IRBuildFactory f = IRBuildFactory.getInstance();





    private void visitStmtAST(StmtAST stmtAST){
        RetInst inst = new RetInst("ret", );

    }

    private void visitBlockAST(BlockAST blockAST){
        BasicBlock block = new BasicBlock();

        RetInst inst = IRParseRetInst(blockAST.getStmtAST());
        inst.setParentbb(block);
        block.addInst(inst);

        return block;
    }

    private void visitFuncDefAST(FuncDefAST funcDefAST){
        String name = funcDefAST.getIdent();
        String type_t = funcDefAST.getFuncType();
        Type type;

        if(type_t.equals("int")){
            type = new IntegerType(32);
        }
        else type = new VoidType();

        Function function = new Function(name, type);

        ArrayList<BasicBlock> bbs = new ArrayList<>();
        ArrayList<Argument> args = new ArrayList<>();

        BasicBlock bb = IRParseBasicBlock(funcDefAST.getBlockAST());
        bb.setParentFunc(function);
        bbs.add(bb);

        function.setBbs(bbs);
        function.setArgs(args);

        return function;
    }

    public IRModule VisitCompUnit(CompUnitAST compUnitAST){

        IRParseFunction(compUnitAST.getFuncDefAST());

        return f.getModule();
    }
}
