package IR;

import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;
import IR.Type.*;
import IR.Value.*;
import IR.Value.Instructions.*;
import Utils.ErrDump;

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
    //  将globalVars设成全局是为了方便所有visitDecl，visitDef向这里添加globalVars
    private final ArrayList<GlobalVar> globalVars = new ArrayList<>();
    private BasicBlock CurBasicBlock;
    private Value CurValue;
    private OP CurOP;

    private OP StrToOP(String str){
        switch (str) {
            case "+":
                return OP.Add;
            case "-":
                return OP.Sub;
            case "*":
                return OP.Mul;
            case "/":
                return OP.Div;
            case "%":
                return OP.Mod;
            case "==":
                return OP.Eq;
            case "!=":
                return OP.Ne;
            case ">":
                return OP.Gt;
            case "<":
                return OP.Lt;
            case ">=":
                return OP.Ge;
            case "<=":
                return OP.Le;
        }
        return null;
    }

    //  变量数组初始化时用
    private ArrayList<Value> fillInitVal = new ArrayList<>();
    private IRModule module;

    //  isFuncRParam表示当前是否为访问FuncRParam的模式
    //  之所以不用boolean是因为FuncRParam可能会有嵌套，我们通过加减来实现栈的效果
    private int isFuncRParam = 0;
    //  用来记录printf/scanf语句出现的次数，从而为fString命名
    private int strNum = 0;

    //  用于cmp指令前的检查，将目标Value转换为i32,被用在compType中
    private Value checkType(Value value){
        if(value.getType().isIntegerTy()){
            IntegerType integerType = (IntegerType) value.getType();
            int bit = integerType.getBit();
            if(bit == 1){
                return f.buildConversionInst(OP.Zext, value, CurBasicBlock);
            }
        }
        return value;
    }

    //  由于compType全是在CurValue和TmpValue之间使用
    //  因此我们就返回一个Value作为TmpValue的新值
    //  CurValue在函数内就可以改变
    private Value compType(Value tmp){
        //  构建cmp指令之前先检查类型
        //  构建cmp指令之前先检查类型
        IntegerType tmpType = (IntegerType) tmp.getType();
        IntegerType curType = (IntegerType) CurValue.getType();
        if(tmpType.getBit() != curType.getBit()){
            if(curType.getBit() == 1){
                CurValue = checkType(CurValue);
                return tmp;
            }
            else return checkType(tmp);
        }
        return tmp;
    }

    //  LVal有取出值的情况，fetchVal函数获取一个指针,返回其中的值
    private Value fetchVal(Value value){
        Type type = value.getType();

        if(type.isArrayType()){
            ArrayList<Value> indexs = new ArrayList<>();
            for(int i = 0; i < 2; i++) {
                indexs.add(ConstInteger.constZero);
            }
            return f.buildGepInst(value, indexs, CurBasicBlock);
        }
        else return f.buildLoadInst(value, CurBasicBlock);
    }

    //  判断一个lVal是否为const
    private void dealConstLVal(LValAST lValAST){
        Value value = find(lValAST.getIdent());

        if (value instanceof ConstInteger) {
            ConstInteger constInteger = (ConstInteger) value;
            CurValue = f.buildNumber(constInteger.getVal());
        }
        //  常量数组
        else {
            value = find(lValAST.getIdent() + ";const");
            ConstArray constArray = (ConstArray) value;
            assert constArray != null;
            ArrayList<Integer> dimList = constArray.getDimList();
            ArrayList<Integer> arrayValues = constArray.getArrayValues();

            int dimLen = dimList.size();
            //  这里tmpMulDim用于辅助计算是arrayValues第?个元素
            int idx = 0;
             int[] tmpMulDim = new int[dimLen];
            for(int i = dimLen - 1; i >= 0; i--){
                if(i == dimLen - 1) tmpMulDim[i] = 1;
                else tmpMulDim[i] = dimList.get(i + 1) * tmpMulDim[i + 1];
            }

            ArrayList<ExpAST> expASTS = lValAST.getExpASTS();
            int expLen = expASTS.size();
            for(int i = 0; i < expLen; i++){
                ExpAST expAST = expASTS.get(i);
                visitExpAST(expAST, true);
                int num = Integer.parseInt(CurValue.getName());
                idx += num * tmpMulDim[i];
            }

            CurValue = f.buildNumber(arrayValues.get(idx));
        }
    }
    //  这两个arrayList用于在continue和break时保存while循环入口块和跳出块的信息
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
    //  tmpHashMap用于保存FuncFParams
    //  因为当你访问FuncFParams时，你还没有进入Block，而只有进入Block你才能push新的符号表
    //  所以为了把FuncFParams的声明也放进符号表，我们用tmpHashMap来保存
    private final HashMap<String, Value> tmpHashMap = new HashMap<>();

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
        symTbls.add(new HashMap<>());
    }

    private void popSymTbl(){
        int len = symTbls.size();
        symTbls.remove(len - 1);
    }

    private HashMap<String, Value> getNowSymTbl(){
        int len = symTbls.size();
        return symTbls.get(len - 1);
    }

    private void pushSymbol(String ident, Value value){
        int len = symTbls.size();
        symTbls.get(len - 1).put(ident, value);
    }


    private ConstInteger calValue(int left, OP op, int right){
        switch (op) {
            case Add:
                return new ConstInteger(left + right);
            case Sub:
                return new ConstInteger(left - right);
            case Mul:
                return new ConstInteger(left * right);
            case Div:
                return new ConstInteger(left / right);
            case Mod:
                return new ConstInteger(left % right);
            default:
                return null;
        }
    }

    //  Find方法用于从符号表(们)找目标ident
    //  从当前block对应的符号表逐级向下寻找。
    //  如果没有找到返回null
    private Value find(String ident){
        int len = symTbls.size();
        for(int i = len - 1; i >= 0; i--){
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


    //  mode为1表示令CurValue = value
    //  mode为2表示令CurValue = pointer
    //  其实就是mod为1要多加一下load/gep指令
    private void visitLValAST(LValAST lValAST, int mode, boolean isConst){
        Value value = find(lValAST.getIdent());
        if(value == null){
            ErrDump.error_c(lValAST.getLine());
            //  遇事不决放个0
            CurValue = f.buildNumber(0);
            return;
        }
        //  对常量赋值的错误处理
        if(mode == 2){
            ErrDump.error_h(value, lValAST.getLine());
        }

        Type valueType = value.getType();

        //  特殊处理一下isConstExp
        if(isConst) {
            dealConstLVal(lValAST);
            return;
        }

        if(lValAST.getType() == 1){
            //  lVal为FuncRParam
            if(isFuncRParam != 0 && valueType.isArrayType()){
                if(mode == 1) {
                    CurValue = fetchVal(value);
                }
                else CurValue = value;
            }
            //  lVal为变量或常量
            //  为什么这里lVal还有可能是常量呢？
            //  是因为对于一些Exp如i*const_a，我们传进来的isConst是false
            //  但是const_a显然我们应该直接带入值，因此这里也有可能出现常量
            else{
                if (value instanceof ConstInteger) {
                    ConstInteger constInteger = (ConstInteger) value;
                    CurValue = f.buildNumber(constInteger.getVal());
                }
                else {
                    CurValue = value;
                    if (mode == 1) {
                        CurValue = fetchVal(CurValue);
                    }
                }
            }
        }
        //  在数组的情况下 有个问题就是要考虑lVal为参数的情况
        //  比如void solve(int a[][2])
        //  我们存的a的类型并不是[2 x [2 x i32]]*
        //  因为我们不知道a的第一维长度，所以我们存的是等价的[2 x i32]**
        //  这会导致我们改变构建gep的方式：
        //  这种情况下，我们采取先load出[2 x i32]*，在构建没有第一个0的gep指令
        else if(lValAST.getType() == 2){
            ArrayList<Value> indexs = new ArrayList<>();

            //  判断是否为一个数组**或i32**
            boolean isFuncLParam = false;
            if(!valueType.isArrayType() && valueType.isPointerType()){
                PointerType pointerType = (PointerType) valueType;
                Type elementType = pointerType.getEleType();
                if(elementType.isPointerType()){
                    isFuncLParam = true;
                }
            }

            //  FuncLParam采用load， 正常数组gep多建一个0
            if(isFuncLParam){
                value = f.buildLoadInst(value, CurBasicBlock);
            }
            else {
                indexs.add(ConstInteger.constZero);
            }

            //  到这无论是FuncFParam还是正常定义的数组应该都是正常的ArrayType
            ArrayList<ExpAST> expASTS = lValAST.getExpASTS();
            for(ExpAST expAST : expASTS){
                visitExpAST(expAST, false);
                indexs.add(CurValue);
            }

            GepInst gepInst = f.buildGepInst(value, indexs, CurBasicBlock);
            CurValue = gepInst;
            if(mode == 1){
                CurValue = fetchVal(gepInst);
            }
        }
    }

    private void visitPrimaryExpAST(PrimaryExpAST primaryExpAST, boolean isConstExp){
        if(!isConstExp) {
            if (primaryExpAST.getType() == 1) {
                visitExpAST(primaryExpAST.getExpAST(), false);
            } else if(primaryExpAST.getType() == 2){
                visitNumberAST(primaryExpAST.getNumberAST());
            }
            else if(primaryExpAST.getType() == 3){
                visitLValAST(primaryExpAST.getlValAST(), 1, false);
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
                visitLValAST(primaryExpAST.getlValAST(), 1, true);
            }
        }
    }

    private void visitUnaryExpAST(UnaryExpAST unaryExpAST, boolean isConstExp){
        int line = unaryExpAST.getLine();
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
                        CurValue = f.buildCmpInst(ConstInteger.constZero, CurValue, OP.Eq, CurBasicBlock);
                        break;
                }
            }
            //  Ident (FuncRParam)  !!很关键(处理数组参数)
            else if(unaryExpAST.getType() == 3 || unaryExpAST.getType() == 4){
                String funcName = unaryExpAST.getIdent();
                Value findFunc = find(funcName);
                if(findFunc == null){
                    ErrDump.error_c(line);
                    //  遇事不决放个0
                    CurValue = f.buildNumber(0);
                    return;
                }
                Function function = (Function) findFunc;

                if(unaryExpAST.getType() == 3) {
                    //  开始处理FuncRParam
                    ArrayList<Value> values = new ArrayList<>();
                    ArrayList<ExpAST> expASTS = unaryExpAST.getFuncRParamsAST().getExpASTS();
                    ErrDump.error_d(function, expASTS.size(), line);

                    isFuncRParam++;
                    for (ExpAST expAST : expASTS) {
                        visitExpAST(expAST, false);
                        values.add(CurValue);
                    }
                    isFuncRParam--;

                    ErrDump.error_e(function, values, line);

                    CurValue = f.buildCallInst(CurBasicBlock, function, values);
                }
                else {
                    ErrDump.error_d(function, 0, line);
                    CurValue = f.buildCallInst(CurBasicBlock, function);
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
                CurValue = calValue(0, StrToOP(unaryExpAST.getUnaryOP()), constInteger.getVal());
            }
        }
    }

    private void visitExpAST(ExpAST expAST, boolean isConst){
        CurValue = null;
        CurOP = null;
        visitAddExpAST(expAST.getAddExpAST(), isConst);
    }

    private void visitAddExpAST(AddExpAST addExpAST, boolean isConstExp){
        if(!isConstExp) {
            Value TmpValue = CurValue;
            OP TmpOP = CurOP;
            CurValue = null;
            CurOP = null;
            visitMulExpAST(addExpAST.getMulExpAST(), false);
            //  TmpValue用于保存上一层传进来的用于计算Value
            //  如果为null说明是最左边的第一次递归
            if(TmpValue != null){
                if(!TmpValue.getType().isPointerType() && !CurValue.getType().isPointerType()) {
                    //  构建运算之前先compType
                    TmpValue = compType(TmpValue);
                    CurValue = f.buildBinaryInst(TmpOP, TmpValue, CurValue, CurBasicBlock);
                }
                else {
                    Value target, index;
                    if(TmpValue.getType().isPointerType()){
                        target = TmpValue;
                        index = CurValue;
                    }
                    else{
                        target = CurValue;
                        index = TmpValue;
                    }
                    ArrayList<Value> indexs = new ArrayList<>();
                    indexs.add(index);

                    CurValue = f.buildGepInst(target, indexs, CurBasicBlock);
                }

            }
            if (addExpAST.getType() != 1) {
                CurOP = StrToOP(addExpAST.getOp());
                visitAddExpAST(addExpAST.getAddExpAST(), false);
            }
        }
        else{
            ConstInteger left = (ConstInteger) CurValue;
            OP TmpOP = CurOP;
            CurValue = null;
            CurOP = null;
            visitMulExpAST(addExpAST.getMulExpAST(), true);
            ConstInteger right = (ConstInteger) CurValue;

            if(left != null){
                CurValue = calValue(left.getVal(), TmpOP, right.getVal());
            }
            if(addExpAST.getType() == 2){
                CurOP = StrToOP(addExpAST.getOp());
                visitAddExpAST(addExpAST.getAddExpAST(), true);
            }
        }
    }

    private void visitMulExpAST(MulExpAST mulExpAST, boolean isConstExp){
        if(!isConstExp) {
            Value TmpValue = CurValue;
            OP TmpOP = CurOP;
            CurValue = null;
            CurOP = null;
            visitUnaryExpAST(mulExpAST.getUnaryExpAST(), false);
            if(TmpValue != null){
                TmpValue = compType(TmpValue);
                CurValue = f.buildBinaryInst(TmpOP, TmpValue, CurValue, CurBasicBlock);
            }
            if (mulExpAST.getType() == 2) {
                CurOP = StrToOP(mulExpAST.getOp());
                visitMulExpAST(mulExpAST.getMulExpAST(), false);
            }
        }
        else{
            ConstInteger left = (ConstInteger) CurValue;
            OP TmpOP = CurOP;
            CurValue = null;
            CurOP = null;
            visitUnaryExpAST(mulExpAST.getUnaryExpAST(), true);
            ConstInteger right = (ConstInteger) CurValue;
            if(left != null){
                CurValue = calValue(left.getVal(), TmpOP, right.getVal());
            }
            if(mulExpAST.getType() == 2) {
                CurOP = StrToOP(mulExpAST.getOp());
                visitMulExpAST(mulExpAST.getMulExpAST(), true);
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
        Value TmpValue = CurValue;
        OP TmpOP = CurOP;
        CurValue = null;
        CurOP = null;
        visitAddExpAST(relExpAST.getAddExpAST(), false);

        if(TmpValue != null){
            TmpValue = compType(TmpValue);
            CurValue = f.buildCmpInst(TmpValue, CurValue, TmpOP, CurBasicBlock);
        }
        if(relExpAST.getType() == 2){
            CurOP = StrToOP(relExpAST.getOp());
            visitRelExpAST(relExpAST.getRelExpAST());
        }
    }

    private void visitEqExpAST(EqExpAST eqExpAST){
        Value TmpValue = CurValue;
        OP TmpOP = CurOP;
        CurValue = null;
        CurOP = null;
        visitRelExpAST(eqExpAST.getRelExpAST());

        if(TmpValue != null){
            TmpValue = compType(TmpValue);
            CurValue = f.buildCmpInst(TmpValue, CurValue, TmpOP, CurBasicBlock);
        }

        if(eqExpAST.getType() == 2){
            CurOP = StrToOP(eqExpAST.getOp());
            visitEqExpAST(eqExpAST.getEqExpAST());
        }
    }

    private void visitLAndExpAST(LAndExpAST lAndExpAST, BasicBlock TrueBLock, BasicBlock FalseBlock){
        CurValue = null;
        CurOP = null;
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
        CurValue = null;
        CurOP = null;
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
            visitLValAST(stmtAST.getLValAST(), 2, false);
            f.buildStoreInst(CurBasicBlock, value, CurValue);
        }
        //  Block
        else if(stmtAST.getType() == 3){
            visitBlockAST(stmtAST.getBlockAST(), false);
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
            if(whileEntryBLocks.size() == 0) return;

            BasicBlock whileEntryBlock = getWhileEntry();
            f.buildBrInst(whileEntryBlock, CurBasicBlock);
            CurBasicBlock = f.buildBasicBlock(CurFunction);
        }
        //  break;
        else if(stmtAST.getType() == 9){
            if(whileOutBlocks.size() == 0) return;

            BasicBlock whileOutBlock = getWhileOut();
            f.buildBrInst(whileOutBlock, CurBasicBlock);
            CurBasicBlock = f.buildBasicBlock(CurFunction);
        }
        //  LVal = getint();
        else if(stmtAST.getType() == 10){
            Function scanfFunc = new Function("@__isoc99_scanf", new IntegerType(32));
            ArrayList<Value> rParams = new ArrayList<>();
            String fString = "%d\\00";
            strNum++;
            String strName = "@.str." + strNum;
            Value fStrValue = new Value(strName, new StringType(fString, 1));
            f.buildGlobalVar(strName, fStrValue.getType(),false, null, globalVars);
            rParams.add(fStrValue);

            //  LVal获得变量的Value
            visitLValAST(stmtAST.getLValAST(), 2, false);
            rParams.add(CurValue);
            //  此时CurValue是LVal

            f.buildCallInst(CurBasicBlock, scanfFunc, rParams);
        }
        //  printf
        else if(stmtAST.getType() == 11){
            Function printfFunc = new Function("@printf", new IntegerType(32));
            ArrayList<Value> rParams = new ArrayList<>();
            ArrayList<ExpAST> expASTS = stmtAST.getExpASTS();
            String fString = stmtAST.getfString();
            strNum++;
            String strName = "@.str." + strNum;
            //  这里新建了一个StringType，属于是为了完成printf而自己新建的
            //  虽然不破坏整体的架构，但总感觉有点别扭(不过能跑就行x
            Value fStrValue = new Value(strName, new StringType(fString, 0));
            f.buildGlobalVar(strName, fStrValue.getType(),false, null, globalVars);
            rParams.add(fStrValue);
            for(ExpAST expAST : expASTS){
                visitExpAST(expAST, false);
                rParams.add(CurValue);
            }
            f.buildCallInst(CurBasicBlock, printfFunc, rParams);
        }
        //  return ;
        else if(stmtAST.getType() == 12){
            f.buildRetInst(CurBasicBlock);
        }
    }

    private void visitConstExpAST(ConstExpAST constExpAST){
        CurValue = null;
        CurOP = null;
        visitAddExpAST(constExpAST.getAddExpAST(),true);
    }

    private void visitConstInitValAST(ConstInitValAST constInitValAST){
        if(constInitValAST.getType() == 1) {
            visitConstExpAST(constInitValAST.getConstExpAST());
            fillInitVal.add(CurValue);
        }
        else if(constInitValAST.getType() == 2){
            ArrayList<ConstInitValAST> constInitValASTS = constInitValAST.getConstInitValASTS();
            for(ConstInitValAST constInitValAST1 : constInitValASTS){
                visitConstInitValAST(constInitValAST1);
            }
        }
    }

    private void visitConstDefAST(ConstDefAST constDefAST, boolean isGlobal){
        String rawIdent = constDefAST.getIdent();
        ErrDump.error_b(rawIdent, getNowSymTbl(), constDefAST.getLen());

        int cnt = addSymCnt(rawIdent);
        String ident = "@" + rawIdent + "_" + cnt;

        if(constDefAST.getType() == 1) {
            visitConstInitValAST(constDefAST.getConstInitValAST());
            if (isGlobal) f.buildGlobalVar(ident, new IntegerType(32),true, CurValue, globalVars);
            pushSymbol(rawIdent, CurValue);
        }
        //  数组
        else if(constDefAST.getType() == 2){
            //  构建dimList
            int totDim = 1;
            ArrayList<Integer> dimList = new ArrayList<>();
            ArrayList<ConstExpAST> constExpASTS = constDefAST.getConstExpASTS();

            for (ConstExpAST constExpAST : constExpASTS) {
                visitConstExpAST(constExpAST);
                int dim = Integer.parseInt(CurValue.getName());
                dimList.add(dim);
                totDim = totDim * dim;
            }

            //  ConstDef一定有InitVal
            fillInitVal = new ArrayList<>();
            //  构建InitValS

            //  ConstArray用于ConstExp查值
            visitConstInitValAST(constDefAST.getConstInitValAST());
            ArrayList<Integer> arrayValue = new ArrayList<>();
            for(Value value : fillInitVal){
                arrayValue.add(Integer.parseInt(value.getName()));
            }

            ConstArray constArray = new ConstArray(ident, dimList, arrayValue);
            //  添加;const来保证不会与ident重合，同时存下数组的初始值一遍直接解出初始值
            pushSymbol(rawIdent + ";const", constArray);

            //  buildArray用于constArray[某变量i]时构建gep等指令
            if(isGlobal){
                GlobalVar globalVar = f.buildGlobalVar(ident, dimList, fillInitVal, true, globalVars);
                pushSymbol(rawIdent, globalVar);
            }
            else{
                AllocInst allocInst = f.buildArray(ident, dimList, CurBasicBlock, true);
                ArrayList<Value> indexs = new ArrayList<>();
                int dim = dimList.size();
                for (int i = 0; i < dim + 1; i++) {
                    indexs.add(ConstInteger.constZero);
                }
                GepInst pointer = f.buildGepInst(allocInst, indexs, CurBasicBlock);

                //  构建memset指令
                Function memsetFunc = new Function("@memset", new VoidType());
                ArrayList<Value> rParams = new ArrayList<>();
                rParams.add(pointer);
                rParams.add(ConstInteger.constZero);
                rParams.add(new ConstInteger(4 * totDim));
                f.buildCallInst(CurBasicBlock, memsetFunc, rParams);

                //  这里的itPointer用作获取每次建立的GepInst，从而构建Store指令
                storeArrayInit(pointer);
                pushSymbol(rawIdent, allocInst);
            }

        }
    }

    private void storeArrayInit(GepInst pointer) {
        GepInst itPointer = pointer;
        for (int i = 0; i < fillInitVal.size(); i++) {
            //  重新构建gep所需的indexs
            ArrayList<Value> itIndexs = new ArrayList<>();
            itIndexs.add(new ConstInteger(i));
            if (i != 0) {
                itPointer = f.buildGepInst(pointer, itIndexs, CurBasicBlock);
            }

            f.buildStoreInst(CurBasicBlock, fillInitVal.get(i), itPointer);
        }
    }

    private void visitConstDeclAST(ConstDeclAST constDeclAST, boolean isGlobal){
        ArrayList<ConstDefAST> constDefASTS = constDeclAST.getConstDefASTS();
        for(ConstDefAST constDefAST : constDefASTS){
            visitConstDefAST(constDefAST, isGlobal);
            fillInitVal = new ArrayList<>();
        }
    }

    private void visitDeclAST(DeclAST declAST, boolean isGlobal){
        if(declAST.getType() == 1) visitConstDeclAST(declAST.getConstDeclAST(), isGlobal);
        else visitVarDeclAST(declAST.getVarDeclAST(), isGlobal);
    }

    private void visitVarDeclAST(VarDeclAST varDeclAST, boolean isGlobal){
        ArrayList<VarDefAST> varDefASTS = varDeclAST.getVarDefASTS();
        for(VarDefAST varDefAST : varDefASTS){
            visitVarDefAST(varDefAST, isGlobal);
            fillInitVal = new ArrayList<>();
        }
    }

    private void visitVarDefAST(VarDefAST varDefAST, boolean isGlobal){
        //  这里rawIdent指的是未加@，cnt之类的ident(纯用户命名的ident)
        String rawIdent = varDefAST.getIdent();
        ErrDump.error_b(rawIdent, getNowSymTbl(), varDefAST.getLine());
        int varDefType = varDefAST.getType();
        int cnt = addSymCnt(rawIdent);
        String ident = "@" + rawIdent + "_" + cnt;

        if(isGlobal){
            if(varDefType == 1 || varDefType == 2) {
                if (varDefAST.getType() == 2) visitInitValAST(varDefAST.getInitValAST(), true);
                else CurValue = ConstInteger.constZero;
                CurValue = f.buildGlobalVar(ident, new PointerType(new IntegerType(32)),false, CurValue, globalVars);
                pushSymbol(rawIdent, CurValue);
            }
            //  全局数组
            else {
                //  totDim用于记录将数组展成一维有多少个元素，便于后续算字节数，构建指令
                int totDim = 1;
                //  访问ConstExpAST的List得到CurValue(肯定是ConstInteger)
                //  这些CurValue的值就是数组的维度，放进一个dimList里
                ArrayList<Integer> dimList = new ArrayList<>();
                ArrayList<ConstExpAST> constExpASTS = varDefAST.getConstExpASTS();

                for(ConstExpAST constExpAST : constExpASTS){
                    visitConstExpAST(constExpAST);
                    int dim = Integer.parseInt(CurValue.getName());
                    dimList.add(dim);
                    totDim = totDim * dim;
                }
                //构建InitValS
                fillInitVal = new ArrayList<>();
                GlobalVar globalVar;
                if(varDefAST.getType() == 4){
                    visitInitValAST(varDefAST.getInitValAST(), true);
                }
                else {
                    //  如果没有显式初始化就填充0
                    for(int i = 0; i < totDim; i++){
                        fillInitVal.add(ConstInteger.constZero);
                    }
                }

                globalVar = f.buildGlobalVar(ident, dimList, fillInitVal, false,globalVars);
                pushSymbol(rawIdent, globalVar);
            }
        }

        else {
            if(varDefType == 1 || varDefType == 2) {
                AllocInst allocInst = f.buildAllocInst(ident, new IntegerType(32), CurBasicBlock, false);
                if (varDefType == 2) {
                    visitInitValAST(varDefAST.getInitValAST(), false);
                    f.buildStoreInst(CurBasicBlock, CurValue, allocInst);
                }
                //  未赋初值的全局变量直接置为0
                else f.buildStoreInst(CurBasicBlock, ConstInteger.constZero, allocInst);

                pushSymbol(rawIdent, allocInst);
            }
            //  数组
            else{
                //  totDim用于记录将数组展成一维有多少个元素，便于后续算字节数，构建指令
                int totDim = 1;
                //  访问ConstExpAST的List得到CurValue(肯定是ConstInteger)
                //  这些CurValue的值就是数组的维度，放进一个dimList里
                ArrayList<Integer> dimList = new ArrayList<>();
                ArrayList<ConstExpAST> constExpASTS = varDefAST.getConstExpASTS();

                for(ConstExpAST constExpAST : constExpASTS){
                    visitConstExpAST(constExpAST);
                    int dim = Integer.parseInt(CurValue.getName());
                    dimList.add(dim);
                    totDim = totDim * dim;
                }

                AllocInst allocInst = f.buildArray(ident, dimList, CurBasicBlock, false);
                pushSymbol(rawIdent, allocInst);

                ArrayList<Value> indexs = new ArrayList<>();
                int dim = dimList.size();
                for(int i = 0; i < dim + 1; i++){
                    indexs.add(ConstInteger.constZero);
                }
                GepInst pointer = f.buildGepInst(allocInst, indexs, CurBasicBlock);

                //  构建memset指令
                Function memsetFunc = new Function("@memset", new VoidType());
                ArrayList<Value> rParams = new ArrayList<>();
                rParams.add(pointer);
                rParams.add(ConstInteger.constZero);
                rParams.add(new ConstInteger(4 * totDim));
                f.buildCallInst(CurBasicBlock, memsetFunc, rParams);

                //  有初始值的数组
                if(varDefType == 4){
                    fillInitVal = new ArrayList<>();
                    visitInitValAST(varDefAST.getInitValAST(), false);

                    //  这里的itPointer用作获取每次建立的GepInst，从而构建Store指令
                    storeArrayInit(pointer);
                }
            }

        }
    }

    private void visitInitValAST(InitValAST initValAST, boolean isCal){
        if(initValAST.getType() == 1) {
            visitExpAST(initValAST.getExpAST(), isCal);
            //  反正不是数组的时候也不影响 就放到这里了
            fillInitVal.add(CurValue);
        }
        //  数组初始化
        else if (initValAST.getType() == 2){
            ArrayList<InitValAST> initValASTS = initValAST.getInitValASTS();
            for(InitValAST initValAST1 : initValASTS){
                visitInitValAST(initValAST1, isCal);
            }
        }
    }

    private void visitBlockItemAST(BlockItemAST blockItemAST){
        if(blockItemAST.getType() == 1){
            visitDeclAST(blockItemAST.getDeclAST(), false);
        }
        else if(blockItemAST.getType() == 2){
            visitStmtAST(blockItemAST.getStmtAST());
        }
    }

    private void visitBlockAST(BlockAST blockAST, boolean isEntry){
        pushSymTbl();
        //  当基本块是入口基本块时 构建Alloc指令
        //  再把Alloc的Value放到该函数的HashMap中
        //  为了保证这些参数表示的值和普通的值一样
        if(isEntry) {
            for (String identArg : tmpHashMap.keySet()) {
                Value argument = tmpHashMap.get(identArg);
                AllocInst allocInst = f.buildAllocInst(identArg, argument.getType(), CurBasicBlock, false);
                f.buildStoreInst(CurBasicBlock, argument, allocInst);
                pushSymbol(identArg, allocInst);
            }
        }

        ArrayList<BlockItemAST> blockItemASTS = blockAST.getBlockItems();
        for(BlockItemAST blockItemAST : blockItemASTS){
            visitBlockItemAST(blockItemAST);
        }

        popSymTbl();
    }

    private void visitFuncDefAST(FuncDefAST funcDefAST){
        String ident = funcDefAST.getIdent();
        ErrDump.error_b(ident, getNowSymTbl(), funcDefAST.getLine());

        String type = funcDefAST.getFuncType();
        int line = funcDefAST.getLine();

        CurFunction = f.buildFunction("@" + ident, type, module);
        CurBasicBlock = f.buildBasicBlock(CurFunction);
        //  进入一个新函数后命名要重新开始
        Value.valNumber = -1;
        fillInitVal = new ArrayList<>();

        pushSymbol(ident, CurFunction);

        tmpHashMap.clear();
        //  Has FuncFParams
        if(funcDefAST.getType() == 2){
            FuncFParamsAST funcFParamsAST = funcDefAST.getFuncFParamsAST();
            ArrayList<FuncFParamAST> funcFParamASTS = funcFParamsAST.getFuncFParamASTS();
            for(FuncFParamAST funcFParamAST : funcFParamASTS){
                //  平平无奇的起名环节
                String rawIdentArg = funcFParamAST.getIdent();
                ErrDump.error_b(rawIdentArg, tmpHashMap, line);

                String typeArg = funcFParamAST.getbType();

                int cnt = addSymCnt(rawIdentArg);
                String identArg = "%" + rawIdentArg + "_" + cnt;

                //  平平无奇的普通参数环节
                if(funcFParamAST.getType() == 1) {
                    Argument argument = f.buildArgument(identArg, typeArg, CurFunction);
                    tmpHashMap.put(rawIdentArg, argument);
                }
                //  相当有趣的数组参数环节
                else if(funcFParamAST.getType() == 2){
                    //  构建dimList
                    ArrayList<Integer> dimList = new ArrayList<>();
                    ArrayList<ConstExpAST> constExpASTS = funcFParamAST.getConstExpASTS();
                    for(ConstExpAST constExpAST : constExpASTS){
                        visitConstExpAST(constExpAST);
                        dimList.add(Integer.parseInt(CurValue.getName()));
                    }

                    Argument argument = f.buildArgument(identArg, dimList, CurFunction);

                    tmpHashMap.put(rawIdentArg, argument);
                }
            }
        }

        visitBlockAST(funcDefAST.getBlockAST(), true);

        //  visitBlock之后，我们要检查一下每个block是否只有一条跳转指令
        //  不然sb llvm编译过不了(震怒x
        ArrayList<BasicBlock> bbs = CurFunction.getBbs();
        for(BasicBlock bb : bbs){
            boolean isTerminal = false;
            ArrayList<Instruction> insts = bb.getInsts();
            int len = insts.size();
            for(int i = 0; i < len; i++){
                Instruction inst = insts.get(i);
                if(isTerminal){
                    bb.removeInst(inst);
                    //  之所以这么做是因为对bb中insts的操作会同步到inst里面
                    //  而如果我们不改变len和i的值，凭空让insts少了一条指令
                    //  一定会引起后面i超出范围，从而get方法报错
                    len--; i--;
                }
                else{
                    if(inst instanceof RetInst || inst instanceof BrInst){
                        isTerminal = true;
                    }
                }
            }

            //  如果没有ret语句，构建一个ret void
            if(!isTerminal){
                f.buildRetInst(CurBasicBlock);
            }
        }

    }

    public IRModule VisitCompUnit(CompUnitAST compUnitAST){
        ArrayList<Function> functions = new ArrayList<>();

        module = new IRModule(functions, globalVars);
        //  构建全局域
        pushSymTbl();

        ArrayList<DeclAST> declASTS = compUnitAST.getDeclASTS();
        for(DeclAST declAST : declASTS){
            visitDeclAST(declAST, true);
        }


        ArrayList<FuncDefAST> funcDefASTS = compUnitAST.getFuncDefASTS();
        for (FuncDefAST funcDefAST : funcDefASTS) {
            visitFuncDefAST(funcDefAST);
        }

        return module;
    }
}
