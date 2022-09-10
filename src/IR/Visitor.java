package IR;

import Frontend.AST.*;
import Frontend.AST.ExpAST.*;
import IR.Value.*;
import IR.Value.Instructions.OP;

import java.util.ArrayList;

/*
* Visitor类中一些值的说明
*
* CurValue:在visit到一些特定节点时，需要保存当前Value，以便后续构建IR的某个结构
* TmpValue:在二元指令涉及两个变量时，我们要先visit两次其他节点来获得两个变量，这时一个CurValue就不够用了
* 所以这里创建了一个TmpValue来帮忙存储Value
* */

public class Visitor {
    private final IRBuildFactory f = IRBuildFactory.getInstance();
    private ArrayList<Function> functions;
    private ArrayList<GlobalVars> globalVars;
    private Function CurFunction;
    private BasicBlock CurBasicBlock;
    private Value CurValue;


    private void visitNumberAST(NumberAST numberAST){
        CurValue = f.buildNumber(numberAST.getIntConst());
    }

    private void visitPrimaryExpAST(PrimaryExpAST primaryExpAST){
        if(primaryExpAST.getType() == 1){
            visitExpAST(primaryExpAST.getExpAST());
        }
        else{
            visitNumberAST(primaryExpAST.getNumberAST());
        }
    }

    private void visitUnaryExpAST(UnaryExpAST unaryExpAST){
        if(unaryExpAST.getType() == 1){
            visitPrimaryExpAST(unaryExpAST.getPrimaryExpAST());
        }
        else{
            visitUnaryExpAST(unaryExpAST.getUnaryExpAST());
            switch (unaryExpAST.getUnaryOP()) {
                case "+":

                    break;
                case "-":
                    CurValue = f.buildBinaryInst(OP.Sub, ConstInteger.constZero, CurValue, CurBasicBlock);
                    break;
                case "!":
                    CurValue = f.buildBinaryInst(OP.Eq, ConstInteger.constZero, CurValue, CurBasicBlock);
                    break;
            }
        }
    }

    private void visitExpAST(ExpAST expAST){
        visitAddExpAST(expAST.getAddExpAST());
    }

    private void visitAddExpAST(AddExpAST addExpAST){
        visitMulExpAST(addExpAST.getMulExpAST());
        if(addExpAST.getType() != 1){
            Value TmpValue = CurValue;
            visitAddExpAST(addExpAST.getAddExpAST());
            if(addExpAST.getOp().equals("+")){
                CurValue = f.buildBinaryInst(OP.Add, TmpValue, CurValue, CurBasicBlock);
            }
            else if(addExpAST.getOp().equals("-")){
                CurValue = f.buildBinaryInst(OP.Sub, TmpValue, CurValue, CurBasicBlock);
            }
        }
    }

    private void visitMulExpAST(MulExpAST mulExpAST){
        visitUnaryExpAST(mulExpAST.getUnaryExpAST());

        if(mulExpAST.getType() != 1){
            Value TmpValue = CurValue;
            visitMulExpAST(mulExpAST.getMulExpAST());
            switch (mulExpAST.getOp()) {
                case "*" -> CurValue = f.buildBinaryInst(OP.Mul, TmpValue, CurValue, CurBasicBlock);
                case "/" -> CurValue = f.buildBinaryInst(OP.Div, TmpValue, CurValue, CurBasicBlock);
                case "%" -> CurValue = f.buildBinaryInst(OP.Mod, TmpValue, CurValue, CurBasicBlock);
            }
        }
    }

    private void visitStmtAST(StmtAST stmtAST){

        visitExpAST(stmtAST.getExpAST());
        CurValue = f.buildRetInst(CurBasicBlock, CurValue);
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
