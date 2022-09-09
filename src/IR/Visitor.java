package IR;

import Frontend.AST.*;
import IR.Value.*;
import java.util.ArrayList;

public class Visitor {
    //
    private IRBuildFactory f = IRBuildFactory.getInstance();
    private ArrayList<Function> functions;
    private ArrayList<GlobalVars> globalVars;
    private Function CurFunction;
    private BasicBlock CurBasicBlock;
    private Instruction CurInst;
    private Value CurValue;


    private void visitNumberAST(NumberAST numberAST){
        CurValue = f.buildNumber(numberAST.getIntConst());
    }

    private void visitStmtAST(StmtAST stmtAST){

        visitNumberAST(stmtAST.getNumberAST());
        CurInst = f.buildRetInst(CurBasicBlock, CurValue);
    }

    private void visitBlockAST(BlockAST blockAST){
        CurBasicBlock = f.buildBasicBlock(CurFunction);

        visitStmtAST(blockAST.getStmtAST());

    }

    private void visitFuncDefAST(FuncDefAST funcDefAST){
        String name = "%" + funcDefAST.getIdent();
        String type = funcDefAST.getFuncType();

        CurFunction = f.buildFunction(name, type);
        functions.add(CurFunction);
        visitBlockAST(funcDefAST.getBlockAST());
    }

    public IRModule VisitCompUnit(CompUnitAST compUnitAST){
        functions = new ArrayList<>();
        globalVars = new ArrayList<>();

        visitFuncDefAST(compUnitAST.getFuncDefAST());

        return f.buildModule(functions, globalVars);
    }
}
