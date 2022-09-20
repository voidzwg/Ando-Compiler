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
    private Function CurFunction;
    private BasicBlock CurBasicBlock;

    private Value CurValue;

    private IRModule module;
    //  这两个block用于在continue和break时保存while循环入口块和跳出块的信息
    //  我们这里用数组模拟栈，之所以不能像之前的if/else中在函数中定义block来保存当前true/false block，
    //  是因为在if/else的时候无论是否发生了if/else的嵌套，还是只是单纯的非跳转语句
    //  我们只要在回溯的时候为上一层的block构建br即可
    //  即只需在上一层为操作下一层的数据

    //  而对于while，while内有无continue和break，我们处理方法是不一样的
    //  如果出现break/continue，我们必须要直接构建br指令并切换CurBasicBlock
    //  因此我们不能在while之后统一做，必须在处理break和continue的时候操作
    //  这就导致我们要在递归的下一层用到上一层的数据，这是之前的方法无法实现的
    //  所以我们选择用栈来保存while的嵌套信息
    private final ArrayList<BasicBlock> whileEntryBLocks = new ArrayList<>();
    private final ArrayList<BasicBlock> whileOutBlocks = new ArrayList<>();

    //  符号表
    private final ArrayList<HashMap<String, Value>> symTbls = new ArrayList<>();
    private int symTop = -1;
    //  用于记录标识符出现的次数，以防不同block定义的变量构建Value时重名
    private final HashMap<String, Integer>symCnt = new HashMap<>();

    //  Utils方法
    private int addSymCnt(String ident){
        int cnt = 0;
        if(symCnt.get(ident) == null){
            symCnt.put(ident, 0);
        }
        else{
            cnt = symCnt.get(ident) + 1;
            symCnt.replace(ident, cnt);
        }
        return cnt;
    }

    private void pushWhileEntry(BasicBlock whileEntryBlock){
        whileEntryBLocks.add(whileEntryBlock);
    }

    private void pushWhileOut(BasicBlock whileOutBlock){
        whileOutBlocks.add(whileOutBlock);
    }

    private void popWhileEntry(){
        int len = whileEntryBLocks.size();
        whileEntryBLocks.remove(len - 1);
    }

    private void popWhileOut(){
        int len = whileOutBlocks.size();
        whileOutBlocks.remove(len - 1);
    }

    private BasicBlock getWhileEntry(){
        int len = whileEntryBLocks.size();
        return whileEntryBLocks.get(len - 1);
    }

    private BasicBlock getWhileOut(){
        int len = whileOutBlocks.size();
        return whileOutBlocks.get(len - 1);
    }

    private void pushSymTbl(){
        symTop++;
        if(symTbls.size() == symTop) symTbls.add(new HashMap<>());
    }

    private void popSymTbl(){
        symTop--;
    }

    private void pushSymbol(String ident, Value value){
        symTbls.get(symTop).put(ident, value);
    }


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
            }
            else if(unaryExpAST.getType() == 2){
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
            else if(unaryExpAST.getType() == 3){
                String funcName = unaryExpAST.getIdent();
                Function function = (Function) find(funcName);

                ArrayList<Value> values = new ArrayList<>();
                ArrayList<ExpAST> expASTS = unaryExpAST.getFuncRParamsAST().getExpASTS();
                for(ExpAST expAST : expASTS){
                    visitExpAST(expAST, false);
                    values.add(CurValue);
                }
                CurValue = f.buildCallInst(CurBasicBlock, function, values);
            }
            else if(unaryExpAST.getType() == 4){
                String funcName = unaryExpAST.getIdent();
                Function function = (Function) find(funcName);
                CurValue = f.buildCallInst(CurBasicBlock, function);
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

    private void visitLOrExpAST(LOrExpAST lOrExpAST, BasicBlock TrueBlock, BasicBlock FalseBlock){
        BasicBlock NxtLOrBlock = FalseBlock;
        if(lOrExpAST.getType() == 2) {
            NxtLOrBlock = f.buildBasicBlock(CurFunction);
        }

        visitLAndExpAST(lOrExpAST.getLAndExpAST(), TrueBlock, NxtLOrBlock);

        CurValue = f.buildCmpInst(CurValue, ConstInteger.constZero, OP.Ne, CurBasicBlock);

        f.buildBrInst(CurValue, TrueBlock, NxtLOrBlock, CurBasicBlock);
        if(lOrExpAST.getType() == 2) {
            CurBasicBlock = NxtLOrBlock;
            visitLOrExpAST(lOrExpAST.getLOrExpAST(), TrueBlock, FalseBlock);
        }

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

    private void visitLAndExpAST(LAndExpAST lAndExpAST, BasicBlock TrueBLock, BasicBlock FalseBlock){

        visitEqExpAST(lAndExpAST.getEqExpAST());

        if(lAndExpAST.getType() == 2){
            BasicBlock NxtLAndBlock = f.buildBasicBlock(CurFunction);
            CurValue = f.buildCmpInst(CurValue, ConstInteger.constZero, OP.Ne, CurBasicBlock);
            f.buildBrInst(CurValue, NxtLAndBlock, FalseBlock, CurBasicBlock);

            CurBasicBlock = NxtLAndBlock;
            visitLAndExpAST(lAndExpAST.getLAndExpAST(), TrueBLock, FalseBlock);
        }
    }


    private void visitCondAST(CondAST condAST, BasicBlock TrueBlock, BasicBlock FalseBlock){
        visitLOrExpAST(condAST.getLOrExpAST(), TrueBlock, FalseBlock);
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
            BasicBlock TrueBlock = f.buildBasicBlock(CurFunction);
            BasicBlock NxtBlock = f.buildBasicBlock(CurFunction);
            visitCondAST(stmtAST.getCondAST(), TrueBlock, NxtBlock);
            //  VisitCondAST之后，CurBlock的br已经构建完并指向正确的Block
            //  接下来我们为TrueBlock填写指令
            CurBasicBlock = TrueBlock;
            visitStmtAST(stmtAST.getIfStmtAST());

            //  下面先考虑ifStmt中CurBlock不发生变化的情况
            //  即TrueBlock没有被构建br指令
            //  那么我们显然要给它构建br指令并设置CurBlock为NxtBlock
            //  然后我们发现就算CurBlock发生了变化，那么也是变成了TrueBlock里的NxtBlock
            //  而且是没有终结的状态，因此我们下面两行代码也可以适用于这种情况,令其跳转
            f.buildBrInst(NxtBlock, CurBasicBlock);
            CurBasicBlock = NxtBlock;
        }
        //  if ( Cond ) Stmt else Stmt
        else if(stmtAST.getType() == 6){
            BasicBlock TrueBlock = f.buildBasicBlock(CurFunction);
            BasicBlock FalseBlock = f.buildBasicBlock(CurFunction);
            BasicBlock NxtBlock = f.buildBasicBlock(CurFunction);

            visitCondAST(stmtAST.getCondAST(), TrueBlock, FalseBlock);

            //  构建TrueBlock
            CurBasicBlock = TrueBlock;
            visitStmtAST(stmtAST.getIfStmtAST());

            //  这里原理同上，为CurBlock构建Br指令
            f.buildBrInst(NxtBlock, CurBasicBlock);

            //  开始构建FalseBlock
            CurBasicBlock = FalseBlock;
            visitStmtAST(stmtAST.getElseStmtAST());

            //  原理同上，为CurBLock构建Br指令
            f.buildBrInst(NxtBlock, CurBasicBlock);

            //  最后令CurBlock为NxtBlock
            CurBasicBlock = NxtBlock;
        }
        else if(stmtAST.getType() == 7){
            //  构建要跳转的CurCondBlock
            BasicBlock CurCondBlock = f.buildBasicBlock(CurFunction);
            f.buildBrInst(CurCondBlock, CurBasicBlock);
            CurBasicBlock = CurCondBlock;

            BasicBlock TrueBlock = f.buildBasicBlock(CurFunction);
            BasicBlock FalseBlock = f.buildBasicBlock(CurFunction);
            //  入栈，注意这里entry为CurCondBlock，因为continue要重新判断条件
            pushWhileEntry(CurCondBlock);
            pushWhileOut(FalseBlock);

            visitCondAST(stmtAST.getCondAST(), TrueBlock, FalseBlock);

            CurBasicBlock = TrueBlock;
            visitStmtAST(stmtAST.getLoopStmt());
            f.buildBrInst(CurCondBlock, CurBasicBlock);
            CurBasicBlock = FalseBlock;

            //  while内的指令构建完了，出栈
            popWhileEntry();
            popWhileOut();
        }
        //  continue;
        else if(stmtAST.getType() == 8){
            BasicBlock whileEntryBlock = getWhileEntry();
            f.buildBrInst(whileEntryBlock, CurBasicBlock);
            CurBasicBlock = f.buildBasicBlock(CurFunction);
        }
        //  break;
        else if(stmtAST.getType() == 9){
            BasicBlock whileOutBlock = getWhileOut();
            f.buildBrInst(whileOutBlock, CurBasicBlock);
            CurBasicBlock = f.buildBasicBlock(CurFunction);
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

        pushSymbol(ident, CurValue);
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

        int cnt = addSymCnt(ident);

        AllocInst allocInst = f.buildAllocInst("%" + ident + "_" + cnt, CurBasicBlock);
        if(varDefAST.getType() == 2){
            visitInitValAST(varDefAST.getInitValAST());
            f.buildStoreInst(CurBasicBlock, CurValue, allocInst);
        }
        //  未赋初值的全局变量直接置为0
        else f.buildStoreInst(CurBasicBlock, ConstInteger.constZero, allocInst);

        pushSymbol(ident, allocInst);
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
        pushSymTbl();

        ArrayList<BlockItemAST> blockItemASTS = blockAST.getBlockItems();
        for(BlockItemAST blockItemAST : blockItemASTS){
            visitBlockItemAST(blockItemAST);
        }

        popSymTbl();
    }

    private void visitFuncDefAST(FuncDefAST funcDefAST){
        String ident = funcDefAST.getIdent();
        String type = funcDefAST.getFuncType();
        CurFunction = f.buildFunction("@" + ident, type, module);
        CurBasicBlock = f.buildBasicBlock(CurFunction);

        pushSymbol(ident, CurFunction);

        //  Has FuncFParams
        if(funcDefAST.getType() == 2){
            pushSymTbl();

            FuncFParamsAST funcFParamsAST = funcDefAST.getFuncFParamsAST();
            ArrayList<FuncFParamAST> funcFParamASTS = funcFParamsAST.getFuncFParamASTS();
            for(FuncFParamAST funcFParamAST : funcFParamASTS){
                String identArg = funcFParamAST.getIdent();
                String typeArg = funcFParamAST.getbType();

                int cnt = addSymCnt(identArg);

                Argument argument = f.buildArgument("%" + identArg + "_" + cnt, typeArg, CurFunction);
                pushSymbol(identArg, argument);
            }

            popSymTbl();
        }

        visitBlockAST(funcDefAST.getBlockAST());
    }

    public IRModule VisitCompUnit(CompUnitAST compUnitAST){
        ArrayList<Function> functions = new ArrayList<>();
        ArrayList<GlobalVars> globalVars = new ArrayList<>();

        module = new IRModule(functions, globalVars);
        //  构建全局域
        pushSymTbl();

        ArrayList<FuncDefAST> funcDefASTS = compUnitAST.getFuncDefASTS();
        for (FuncDefAST funcDefAST : funcDefASTS) {
            visitFuncDefAST(funcDefAST);
        }

        return module;
    }
}
