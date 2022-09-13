package IR;

import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;
import IR.Value.*;
import IR.Value.Instructions.AllocInst;
import IR.Value.Instructions.OP;

import java.util.ArrayList;
import java.util.HashMap;

/*
* Visitor类中一些值的说明
*
* CurValue:在visit到一些特定节点时，需要保存当前Value，以便后续构建IR的某个结构
* TmpValue:在二元指令涉及两个变量时，我们要先visit两次其他节点来获得两个变量，这时一个CurValue就不够用了
* 所以这里创建了一个TmpValue来帮忙存储Value
* isConstExp:在解析ConstExp的时候，我们希望能够将表达式的值直接计算出来，放到符号表中，
* 因此我们在parseAddExp的时候
*
* */

public class Visitor {
    private final IRBuildFactory f = IRBuildFactory.getInstance();
    private ArrayList<Function> functions;
    private ArrayList<GlobalVars> globalVars;
    private Function CurFunction;
    private BasicBlock CurBasicBlock;
    //  这两个block在if/else 时使用
    private BasicBlock CurTrueBlock;
    private BasicBlock CurFalseBlock;
    private Value CurValue;

    //  符号表
    private final ArrayList<HashMap<String, Value>> symTbls = new ArrayList<>();
    private int symTop = -1;
    //  用于记录标识符出现的次数，以防不同block定义的变量构建Value时重名
    private final HashMap<String, Integer>symCnt = new HashMap<>();

    //  Utils方法
    private ConstInteger calValue(int left, String op, int right){
        return switch (op) {
            case "+" -> new ConstInteger(left + right);
            case "-" -> new ConstInteger(left - right);
            case "*" -> new ConstInteger(left * right);
            case "/" -> new ConstInteger(left / right);
            case "%" -> new ConstInteger(left % right);
            default -> null;
        };
    }

    //  Find方法用于从符号表(们)找目标ident
    //  从当前block对应的符号表逐级向下寻找。
    //  如果没有找到返回null
    private Value find(String ident){
        for(int i = symTop; i >= 0; i--){
            HashMap<String, Value> symTbl = symTbls.get(i);
            Value res = symTbl.get(ident);
            if(res != null){
                return res;
            }
        }
        return null;
    }


    //  Visitor方法
    private void visitNumberAST(NumberAST numberAST){
        CurValue = f.buildNumber(numberAST.getIntConst());
    }

    private void visitLValAST(LValAST lValAST){
        Value value = find(lValAST.getIdent());

        if(value instanceof ConstInteger) {
            ConstInteger constInteger = (ConstInteger) value;
            CurValue = f.buildNumber(constInteger.getVal());
        }

        else CurValue = value;
    }

    private void visitPrimaryExpAST(PrimaryExpAST primaryExpAST, boolean isConstExp){
        if(!isConstExp) {
            if (primaryExpAST.getType() == 1) {
                visitExpAST(primaryExpAST.getExpAST(), false);
            } else if(primaryExpAST.getType() == 2){
                visitNumberAST(primaryExpAST.getNumberAST());
            }
            else if(primaryExpAST.getType() == 3){
                visitLValAST(primaryExpAST.getlValAST());
                //  判断LVal是常量还是变量
                if(!(CurValue instanceof ConstInteger)) {
                    CurValue = f.buildLoadInst(CurValue, CurBasicBlock);
                }
            }
        }
        else{
            if(primaryExpAST.getType() == 1){
                visitExpAST(primaryExpAST.getExpAST(), true);
            }
            else if(primaryExpAST.getType() == 2){
                NumberAST numberAST = primaryExpAST.getNumberAST();

                CurValue = f.buildNumber(numberAST.getIntConst());
            }
            else if(primaryExpAST.getType() == 3){
                visitLValAST(primaryExpAST.getlValAST());
            }
        }
    }

    private void visitUnaryExpAST(UnaryExpAST unaryExpAST, boolean isConstExp){
        if(!isConstExp) {
            if (unaryExpAST.getType() == 1) {
                visitPrimaryExpAST(unaryExpAST.getPrimaryExpAST(),false);
            } else {
                visitUnaryExpAST(unaryExpAST.getUnaryExpAST(),false);
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
        else {
            if(unaryExpAST.getType() == 1){
                visitPrimaryExpAST(unaryExpAST.getPrimaryExpAST(), true);
            }
            else if(unaryExpAST.getType() == 2){
                visitUnaryExpAST(unaryExpAST.getUnaryExpAST(), true);
                ConstInteger constInteger = (ConstInteger) CurValue;
                CurValue = calValue(0, unaryExpAST.getUnaryOP(), constInteger.getVal());

            }
        }
    }

    private void visitExpAST(ExpAST expAST, boolean isConst){
        visitAddExpAST(expAST.getAddExpAST(), isConst);
    }

    private void visitAddExpAST(AddExpAST addExpAST, boolean isConstExp){
        if(!isConstExp) {
            visitMulExpAST(addExpAST.getMulExpAST(), false);
            if (addExpAST.getType() != 1) {
                Value TmpValue = CurValue;
                visitAddExpAST(addExpAST.getAddExpAST(), false);
                if (addExpAST.getOp().equals("+")) {
                    CurValue = f.buildBinaryInst(OP.Add, TmpValue, CurValue, CurBasicBlock);
                } else if (addExpAST.getOp().equals("-")) {
                    CurValue = f.buildBinaryInst(OP.Sub, TmpValue, CurValue, CurBasicBlock);
                }
            }
        }
        else{
            visitMulExpAST(addExpAST.getMulExpAST(), true);
            if(addExpAST.getType() == 2){
                ConstInteger left = (ConstInteger) CurValue;
                visitAddExpAST(addExpAST.getAddExpAST(), true);
                ConstInteger right = (ConstInteger) CurValue;
                CurValue = calValue(left.getVal(), addExpAST.getOp(), right.getVal());
            }
        }
    }

    private void visitMulExpAST(MulExpAST mulExpAST, boolean isConstExp){
        if(!isConstExp) {
            visitUnaryExpAST(mulExpAST.getUnaryExpAST(), false);

            if (mulExpAST.getType() != 1) {
                Value TmpValue = CurValue;
                visitMulExpAST(mulExpAST.getMulExpAST(), false);
                switch (mulExpAST.getOp()) {
                    case "*" -> CurValue = f.buildBinaryInst(OP.Mul, TmpValue, CurValue, CurBasicBlock);
                    case "/" -> CurValue = f.buildBinaryInst(OP.Div, TmpValue, CurValue, CurBasicBlock);
                    case "%" -> CurValue = f.buildBinaryInst(OP.Mod, TmpValue, CurValue, CurBasicBlock);
                }
            }
        }
        else{
            visitUnaryExpAST(mulExpAST.getUnaryExpAST(), true);
            if(mulExpAST.getType() == 2) {
                ConstInteger left = (ConstInteger) CurValue;
                visitMulExpAST(mulExpAST.getMulExpAST(), true);
                ConstInteger right = (ConstInteger) CurValue;

                CurValue = calValue(left.getVal(), mulExpAST.getOp(), right.getVal());
            }
        }
    }

    private void visitLOrExpAST(LOrExpAST lOrExpAST){
        visitLAndExpAST(lOrExpAST.getLAndExpAST());
        CurValue = f.buildCmpInst(ConstInteger.constZero, CurValue, OP.Ne, CurBasicBlock);
        f.buildBrInst(CurValue, CurTrueBlock, CurFalseBlock, CurBasicBlock);
//        if(lOrExpAST.getType() == 2){
//            Value TmpValue = CurValue;
//            visitLOrExpAST(lOrExpAST.getLOrExpAST());
//
//        }
    }

    private void visitRelExpAST(RelExpAST relExpAST){
        visitAddExpAST(relExpAST.getAddExpAST(), false);
        if(relExpAST.getType() == 2){
            Value TmpValue = CurValue;
            visitRelExpAST(relExpAST.getRelExpAST());
            String op = relExpAST.getOp();
            switch (op) {
                case "<" -> CurValue = f.buildCmpInst(TmpValue, CurValue, OP.Lt, CurBasicBlock);
                case "<=" -> CurValue = f.buildCmpInst(TmpValue, CurValue, OP.Le, CurBasicBlock);
                case ">" -> CurValue = f.buildCmpInst(TmpValue, CurValue, OP.Gt, CurBasicBlock);
                case ">=" -> CurValue = f.buildCmpInst(TmpValue, CurValue, OP.Ge, CurBasicBlock);
            }
        }

    }

    private void visitEqExpAST(EqExpAST eqExpAST){
        visitRelExpAST(eqExpAST.getRelExpAST());
        if(eqExpAST.getType() == 2){
            Value TmpValue = CurValue;
            visitEqExpAST(eqExpAST.getEqExpAST());
            if(eqExpAST.getOp().equals("==")){
                CurValue = f.buildCmpInst(TmpValue, CurValue, OP.Eq, CurBasicBlock);
            }
            else CurValue = f.buildCmpInst(TmpValue, CurValue, OP.Ne, CurBasicBlock);
        }
    }

    private void visitLAndExpAST(LAndExpAST lAndExpAST){
        visitEqExpAST(lAndExpAST.getEqExpAST());

    }


    private void visitCondAST(CondAST condAST){
        CurTrueBlock = f.buildBasicBlock(CurFunction);
        CurFalseBlock = f.buildBasicBlock(CurFunction);
        visitLOrExpAST(condAST.getLOrExpAST());

    }

    private void visitStmtAST(StmtAST stmtAST){
        //  return Exp ;
        if(stmtAST.getType() == 1) {
            visitExpAST(stmtAST.getExpAST(), false);
            CurValue = f.buildRetInst(CurBasicBlock, CurValue);
        }
        //  LVal = Exp;
        else if(stmtAST.getType() == 2){
            visitExpAST(stmtAST.getExpAST(), false);
            //  此时的CurVal为Exp的结果
            Value value = CurValue;
            //  LVal获得变量的Value
            visitLValAST(stmtAST.getLValAST());
            f.buildStoreInst(CurBasicBlock, value, CurValue);
        }
        //  Block
        else if(stmtAST.getType() == 3){
            visitBlockAST(stmtAST.getBlockAST());
        }
        //  [Exp] ;
        else if(stmtAST.getType() == 4){
            if(stmtAST.isHasExp()){
                visitExpAST(stmtAST.getExpAST(), false);
            }
        }
        //  if ( Cond ) Stmt
        else if(stmtAST.getType() == 5){
            visitCondAST(stmtAST.getCondAST());
            CurBasicBlock = CurTrueBlock;
            visitStmtAST(stmtAST.getIfStmtAST());
        }
        //  if ( Cond ) Stmt else Stmt
        else if(stmtAST.getType() == 6){
            visitCondAST(stmtAST.getCondAST());
            CurBasicBlock = CurTrueBlock;
            visitStmtAST(stmtAST.getIfStmtAST());
            CurBasicBlock = CurFalseBlock;
            visitStmtAST(stmtAST.getElseStmtAST());
        }
    }

    private void visitConstExpAST(ConstExpAST constExpAST){
        visitAddExpAST(constExpAST.getAddExpAST(),true);
    }

    private void visitConstInitValAST(ConstInitValAST constInitValAST){
        visitConstExpAST(constInitValAST.getConstExpAST());
    }

    private void visitConstDefAST(ConstDefAST constDefAST){
        String ident = constDefAST.getIdent();

        visitConstInitValAST(constDefAST.getConstInitValAST());

        symTbls.get(symTop).put(ident, CurValue);
    }

    private void visitConstDeclAST(ConstDeclAST constDeclAST){
        ArrayList<ConstDefAST> constDefASTS = constDeclAST.getConstDefASTS();
        for(ConstDefAST constDefAST : constDefASTS){
            visitConstDefAST(constDefAST);
        }
    }

    private void visitDeclAST(DeclAST declAST){
        if(declAST.getType() == 1) visitConstDeclAST(declAST.getConstDeclAST());
        else visitVarDeclAST(declAST.getVarDeclAST());
    }

    private void visitVarDeclAST(VarDeclAST varDeclAST){
        ArrayList<VarDefAST> varDefASTS = varDeclAST.getVarDefASTS();
        for(VarDefAST varDefAST : varDefASTS){
            visitVarDefAST(varDefAST);
        }
    }

    private void visitVarDefAST(VarDefAST varDefAST){
        String ident = varDefAST.getIdent();

        int cnt = 0;
        if(symCnt.get(ident) == null){
            symCnt.put(ident, 0);
        }
        else{
            cnt = symCnt.get(ident) + 1;
            symCnt.replace(ident, cnt);
        }
        AllocInst allocInst = f.buildAllocInst("%" + ident + "_" + cnt, CurBasicBlock);
        if(varDefAST.getType() == 2){
            visitInitValAST(varDefAST.getInitValAST());
            f.buildStoreInst(CurBasicBlock, CurValue, allocInst);
        }
        //  未赋初值的全局变量直接置为0
        else f.buildStoreInst(CurBasicBlock, ConstInteger.constZero, allocInst);

        symTbls.get(symTop).put(ident, allocInst);
    }

    private void visitInitValAST(InitValAST initValAST){
        visitExpAST(initValAST.getExpAST(), false);
    }

    private void visitBlockItemAST(BlockItemAST blockItemAST){
        if(blockItemAST.getType() == 1){
            visitDeclAST(blockItemAST.getDeclAST());
        }
        else if(blockItemAST.getType() == 2){
            visitStmtAST(blockItemAST.getStmtAST());
        }
    }

    private void visitBlockAST(BlockAST blockAST){
        symTop++;
        symTbls.add(new HashMap<>());

        ArrayList<BlockItemAST> blockItemASTS = blockAST.getBlockItems();
        for(BlockItemAST blockItemAST : blockItemASTS){
            visitBlockItemAST(blockItemAST);
        }

        symTop--;
    }

    private void visitFuncDefAST(FuncDefAST funcDefAST){
        String name = "%" + funcDefAST.getIdent();
        String type = funcDefAST.getFuncType();

        CurFunction = f.buildFunction(name, type);
        functions.add(CurFunction);
        CurBasicBlock = f.buildBasicBlock(CurFunction);
        visitBlockAST(funcDefAST.getBlockAST());
    }

    public IRModule VisitCompUnit(CompUnitAST compUnitAST){
        functions = new ArrayList<>();
        globalVars = new ArrayList<>();

        visitFuncDefAST(compUnitAST.getFuncDefAST());

        return f.buildModule(functions, globalVars);
    }
}
