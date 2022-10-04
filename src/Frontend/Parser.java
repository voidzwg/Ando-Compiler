package Frontend;

import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;
import Utils.ErrDump;

import java.io.IOException;
import java.util.ArrayList;

/*
* Parser中变量简介:
* isBack: 用来标记是否有parser函数想要退回上一个Token, 使用于backTok中，是我认为很巧妙的一个思路
* CurTok: 用来存储当前访问到的Tok
* judTok: 函数中命名的judTok一般用于一个非终结符可能有多种方案构成时，用一个judTok来进行判断
*
* */

public class Parser {

    private final ArrayList<Token> CurTok = new ArrayList<>();
    private int cTop = -1;
    private final Lexer lexer;
    //  用来记录while层数，来判断break和continue
    private int whileLv = 0;
    private int CurLine = 0;

    private boolean isExpFirst(Token token){
        Tokens type = token.getType();
        return type == Tokens.IDENFR || type == Tokens.LPARENT || type == Tokens.INTCON || type == Tokens.NOT || type == Tokens.PLUS || type == Tokens.MINU;
    }

    private Token getTok() throws IOException {
        if(cTop == CurTok.size() - 1){
            CurTok.add(lexer.getTok());
        }
        cTop++;
        //  读取token同时读取line
        Token nowTok = CurTok.get(cTop);
        if(nowTok != null) CurLine = nowTok.getLine();
        return nowTok;
    }

    private void backTok(int index){
        cTop-=index;
        if(cTop >= 0) CurLine = CurTok.get(cTop).getLine();
    }

    //  Constructor
    public Parser() throws IOException {
        this.lexer = new Lexer();
    }

    //  Parse Methods
    private NumberAST parseNumberAST() throws IOException {
        Token numberToken = getTok();
        int intConst = Integer.parseInt(numberToken.getVal());

        return new NumberAST(intConst);
    }


    private StmtAST parseStmtAST() throws IOException {
        Token judTok = getTok();   //  Consume 'return'
        int line = judTok.getLine();
        //  "return" Exp ";"
        if(judTok.getVal().equals("return")) {
            line = judTok.getLine();
            judTok = getTok();

            if(judTok.getVal().equals(";")){
                return new StmtAST(line);
            }
            else backTok(1);

            //  单个return缺少分号
            if(!isExpFirst(judTok)){
                ErrDump.error_i(CurLine);
                return new StmtAST(line);
            }

            ExpAST expAST = parseExpAST();

            judTok = getTok();    //  Consume ';'
            //  return Exp缺少分号
            if(!judTok.getVal().equals(";")){
                backTok(1);
                ErrDump.error_i(CurLine);
            }

            return new StmtAST(expAST, line);
        }

        //  Block
        else if(judTok.getVal().equals("{")){
            backTok(1);
            BlockAST blockAST = parseBlockAST();
            return new StmtAST(blockAST);
        }

        else if(judTok.getVal().equals("if")){
            getTok();   //  Consume '('
            CondAST condAST = parseCondAST();

            judTok = getTok();   //  Consume ')'
            if(!judTok.getVal().equals(")")){
                backTok(1);
                ErrDump.error_j(CurLine);
            }

            StmtAST ifStmtAST = parseStmtAST();

            judTok = getTok();
            if(judTok.getVal().equals("else")){
                StmtAST elseStmtAST = parseStmtAST();
                return new StmtAST(ifStmtAST, condAST ,elseStmtAST);
            }

            else {
                backTok(1);
                return new StmtAST(ifStmtAST, condAST);
            }
        }

        else if(judTok.getVal().equals("while")){
            whileLv++;  //  进入循环体

            getTok();   //  Consume '('
            CondAST condAST = parseCondAST();

            judTok = getTok();   //  Consume ')'
            if(!judTok.getVal().equals(")")){
                backTok(1);
                ErrDump.error_j(CurLine);
            }

            StmtAST loopStmt = parseStmtAST();

            whileLv--;  //  退出循环体
            return new StmtAST(condAST, loopStmt);
        }

        else if(judTok.getVal().equals("continue")){
            if(whileLv == 0) ErrDump.error_m(line);

            judTok = getTok();   //  Consume ';'
            if(!judTok.getVal().equals(";")){
                backTok(1);
                ErrDump.error_i(CurLine);
            }

            ContinueAST continueAST = new ContinueAST();
            return new StmtAST(continueAST);
        }

        else if(judTok.getVal().equals("break")){
            if(whileLv == 0) ErrDump.error_m(line);

            judTok = getTok();   //  Consume ';'
            if(!judTok.getVal().equals(";")){
                backTok(1);
                ErrDump.error_i(CurLine);
            }

            BreakAST breakAST = new BreakAST();
            return new StmtAST(breakAST);
        }

        else if(judTok.getVal().equals("printf")){
            getTok();   //  Consume '('
            Token fStringTok = getTok();
            String fString = fStringTok.getVal();

            //  错误处理a:
            ErrDump.error_a(fStringTok);

            ArrayList<ExpAST> expASTS = new ArrayList<>();
            while (true){
                judTok = getTok();
                if(judTok.getVal().equals(")")){
                    break;
                }

                if(judTok.getType() != Tokens.COMMA && !isExpFirst(judTok)){
                    backTok(1);
                    ErrDump.error_j(CurLine);
                    break;
                }

                ExpAST expAST = parseExpAST();
                expASTS.add(expAST);
            }

            ErrDump.error_l(fString, expASTS.size(), line);

            judTok = getTok();   //  Consume ';'
            if(!judTok.getVal().equals(";")){
                backTok(1);
                ErrDump.error_i(CurLine);
            }

            return new StmtAST(fString, expASTS);
        }

        //  LVal "=" Exp ";"    LVal '=' 'getint'
        else if(judTok.getType() == Tokens.IDENFR){
            backTok(1);
            int tmpCTop = cTop;
            LValAST lValAST = parseLValAST();
            judTok = getTok();   //  '=' Or ';'

            if(judTok.getVal().equals("=")) {
                //  说明是'=' 直接Consume掉
                judTok = getTok();
                if (judTok.getVal().equals("getint")) {
                    getTok();   //  Consume '('

                    judTok = getTok();   //  Consume ')'
                    if(!judTok.getVal().equals(")")){
                        backTok(1);
                        ErrDump.error_j(CurLine);
                    }

                    judTok = getTok();   //  Consume ';'
                    if(!judTok.getVal().equals(";")){
                        backTok(1);
                        ErrDump.error_i(CurLine);
                    }

                    return new StmtAST(lValAST, line);
                } else {
                    backTok(1);
                    ExpAST expAST = parseExpAST();

                    judTok = getTok();   //  Consume ';'
                    if(!judTok.getVal().equals(";")){
                        backTok(1);
                        ErrDump.error_i(CurLine);
                    }

                    return new StmtAST(lValAST, expAST, line);
                }
            }
            else {
                cTop = tmpCTop;
                judTok = getTok();
            }
        }
        //  前面都不是才会到这里
        //  [Exp] ;
        if(!judTok.getVal().equals(";")) {
            backTok(1);
            ExpAST expAST = parseExpAST();

            judTok = getTok();   //  Consume ';'
            if(!judTok.getVal().equals(";")){
                backTok(1);
                ErrDump.error_i(CurLine);
            }

            return new StmtAST(expAST, true);
        }
        else return new StmtAST(null, false);
    }

    private CondAST parseCondAST() throws IOException {
        LOrExpAST lOrExpAST = parseLOrExpAST();
        return new CondAST(lOrExpAST);
    }

    private LOrExpAST parseLOrExpAST() throws IOException {
        LAndExpAST lAndExpAST = parseLAndExpAST();
        Token judTok = getTok();
        if(judTok.getType() == Tokens.OR){
            LOrExpAST lOrExpAST = parseLOrExpAST();
            return new LOrExpAST(lAndExpAST, lOrExpAST);
        }
        else {
            backTok(1);
            return new LOrExpAST(lAndExpAST);
        }
    }

    private LAndExpAST parseLAndExpAST() throws IOException {
        EqExpAST eqExpAST = parseEqExpAST();
        Token judTok = getTok();
        if(judTok.getType() == Tokens.AND){
            LAndExpAST lAndExpAST = parseLAndExpAST();
            return new LAndExpAST(eqExpAST, lAndExpAST);
        }
        else {
            backTok(1);
            return new LAndExpAST(eqExpAST);
        }

    }

    private EqExpAST parseEqExpAST() throws IOException {
        RelExpAST relExpAST = parseRelExpAST();
        Token judTok = getTok();
        if(judTok.getType() == Tokens.EQL || judTok.getType() == Tokens.NEQ){
            EqExpAST eqExpAST = parseEqExpAST();
            return new EqExpAST(relExpAST, judTok.getVal(),eqExpAST);
        }
        else {
            backTok(1);
            return new EqExpAST(relExpAST);
        }
    }

    private RelExpAST parseRelExpAST() throws IOException {
        AddExpAST addExpAST = parseAddExpAST();
        Token judTok = getTok();
        String op = judTok.getVal();
        if(op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=")){
            RelExpAST relExpAST = parseRelExpAST();
            return new RelExpAST(addExpAST, op ,relExpAST);
        }
        else{
            backTok(1);
            return new RelExpAST(addExpAST);
        }
    }

    private ExpAST parseExpAST() throws IOException {
        AddExpAST addExp = parseAddExpAST();

        return new ExpAST(addExp);
    }

    private AddExpAST parseAddExpAST() throws IOException {
        MulExpAST mulExpAST = parseMulExpAST();

        Token judTok = getTok();
        if(judTok.getVal().equals("+")){
            AddExpAST addExpAST = parseAddExpAST();
            return new AddExpAST(mulExpAST, "+", addExpAST);
        }
        else if(judTok.getVal().equals("-")){
            AddExpAST addExpAST = parseAddExpAST();
            return new AddExpAST(mulExpAST, "-", addExpAST);
        }

        backTok(1);
        return new AddExpAST(mulExpAST);
    }

    private MulExpAST parseMulExpAST() throws IOException {
        UnaryExpAST unaryExpAST = parseUnaryExpAST();

        Token judTok = getTok();
        String op = judTok.getVal();
        switch (op) {
            case "*": {
                MulExpAST mulExpAST = parseMulExpAST();
                return new MulExpAST(unaryExpAST, "*", mulExpAST);
            }
            case "/": {
                MulExpAST mulExpAST = parseMulExpAST();
                return new MulExpAST(unaryExpAST, "/", mulExpAST);
            }
            case "%": {
                MulExpAST mulExpAST = parseMulExpAST();
                return new MulExpAST(unaryExpAST, "%", mulExpAST);
            }
        }


        backTok(1);
        return new MulExpAST(unaryExpAST);

    }

    private FuncRParamsAST parseFuncRParamsAST() throws IOException {
        ArrayList<ExpAST> expASTS = new ArrayList<>();
        ExpAST expAST = parseExpAST();
        expASTS.add(expAST);
        while (true){
            Token judTok = getTok();
            if(!judTok.getVal().equals(",")){
                backTok(1);
                break;
            }
            else {
                expAST = parseExpAST();
                expASTS.add(expAST);
            }
        }
        return new FuncRParamsAST(expASTS);
    }

    private UnaryExpAST parseUnaryExpAST() throws IOException {
        Token judTok = getTok();

        //  UnaryOP UnaryExp的情况
        if(judTok.getVal().equals("+") || judTok.getVal().equals("-") || judTok.getVal().equals("!")){
            UnaryExpAST unaryExpAST = parseUnaryExpAST();
            return new UnaryExpAST(judTok.getVal(), unaryExpAST, unaryExpAST.getLine());
        }
        else if(judTok.getType() == Tokens.IDENFR){
            int line = judTok.getLine();
            judTok = getTok();
            if(judTok.getVal().equals("(")){
                backTok(2);
                String ident = getTok().getVal();
                getTok();   //  Consume '('

                judTok = getTok();  //  判断有无FuncRParams
                if(!judTok.getVal().equals(")")){
                    backTok(1);
                    //  判断一下无参数同时还缺右括号的情况
                    if(!isExpFirst(judTok)){
                        ErrDump.error_j(CurLine);
                        return new UnaryExpAST(ident, line);
                    }


                    FuncRParamsAST funcRParamsAST = parseFuncRParamsAST();

                    judTok = getTok();   //  Consume ')'
                    if(!judTok.getVal().equals(")")){
                        backTok(1);
                        ErrDump.error_j(CurLine);
                    }

                    return new UnaryExpAST(ident, funcRParamsAST, line);
                }
                else return new UnaryExpAST(ident, line);
            }
            else {
                backTok(2);
                PrimaryExpAST primaryExpAST = parsePrimaryExpAST();
                return new UnaryExpAST(primaryExpAST, line);
            }
        }
        //  不是ident肯定是PrimaryExp的情况
        else {
            backTok(1);
            PrimaryExpAST primaryExpAST = parsePrimaryExpAST();
            return new UnaryExpAST(primaryExpAST, primaryExpAST.getLine());
        }
    }

    private PrimaryExpAST parsePrimaryExpAST() throws IOException {
        Token judTok = getTok();

        //  "(" Exp ")"
        if(judTok.getVal().equals("(")){
            ExpAST expAST = parseExpAST();
            getTok();   //  Consume ')'

            return new PrimaryExpAST(expAST);
        }

        //  LVal
        if(judTok.getType() == Tokens.IDENFR){
            backTok(1);
            LValAST lValAST = parseLValAST();

            return new PrimaryExpAST(lValAST);
        }

        //  Number
        backTok(1);
        NumberAST numberAST = parseNumberAST();

        return new PrimaryExpAST(numberAST);
    }

    private LValAST parseLValAST() throws IOException {
        Token identTok = getTok();
        String ident = identTok.getVal();
        int line = identTok.getLine();
        boolean isArray = false;

        ArrayList<ExpAST> expASTS = new ArrayList<>();
        while (true){
            Token judTok = getTok();
            if(judTok.getVal().equals("[")){
                isArray = true;
                ExpAST expAST = parseExpAST();
                expASTS.add(expAST);

                judTok = getTok();   //  Consume ']'
                if(!judTok.getVal().equals("]")){
                    backTok(1);
                    ErrDump.error_k(CurLine);
                }

            }
            else{
                backTok(1);
                break;
            }
        }

        if(isArray) return new LValAST(ident, expASTS, line);
        return new LValAST(ident, line);
    }

    private BlockAST parseBlockAST() throws IOException {
        getTok();   //  Consume '{'
        int line;
        ArrayList<BlockItemAST> blockItemASTS = new ArrayList<>();
        while (true) {
            Token judTok = getTok();
            if(judTok.getVal().equals("}")){
                line = judTok.getLine();
                break;
            }

            backTok(1);
            BlockItemAST blockItemAST = parseBlockItemAST();
            blockItemASTS.add(blockItemAST);
        }
        return new BlockAST(blockItemASTS, line);
    }

    private BlockItemAST parseBlockItemAST() throws IOException {
        Token judTok = getTok();
        backTok(1);
        if(judTok.getVal().equals("const") || judTok.getVal().equals("int")){
            DeclAST declAST = parseDeclAST();
            return new BlockItemAST(declAST);
        }
        else{
            StmtAST stmtAST = parseStmtAST();
            return new BlockItemAST(stmtAST);
        }
    }

    private DeclAST parseDeclAST() throws IOException {
        Token judTok = getTok();
        backTok(1);
        if(judTok.getVal().equals("const")) {
            ConstDeclAST constDeclAST = parseConstDeclAST();
            return new DeclAST(constDeclAST);
        }
        else {
            VarDeclAST varDeclAST = parseVarDeclAST();
            return new DeclAST(varDeclAST);
        }
    }

    private VarDeclAST parseVarDeclAST() throws IOException {
        Token intTok = getTok();   //  Consume BType 'int'

        VarDeclAST varDeclAST = new VarDeclAST();

        VarDefAST varDefAST = parseVarDefAST();
        varDeclAST.addVarDef(varDefAST);

        while (true){
            Token judTok = getTok();
            if(judTok.getVal().equals(";")){
                break;
            }

            //  错误处理i
            if(!judTok.getVal().equals(",")){
                backTok(1);
                ErrDump.error_i(CurLine);
                break;
            }

            //  这里不用backTok的原因是如果不是';', 那就一定是','. 而','我们也应去除。
            varDefAST = parseVarDefAST();
            varDeclAST.addVarDef(varDefAST);
        }

        return varDeclAST;
    }

    private VarDefAST parseVarDefAST() throws IOException {
        Token identTok = getTok();
        String ident = identTok.getVal();
        int line = identTok.getLine();
        //  这里isArray标记是否为数组的定义
        boolean isArray = false;
        Token judTok = getTok();
        ArrayList<ConstExpAST> constExpASTS = new ArrayList<>();

        if(judTok.getVal().equals("[")){
            do {
                ConstExpAST constExpAST = parseConstExpAST();
                constExpASTS.add(constExpAST);

                judTok = getTok();   //  Consume ']'
                if(!judTok.getVal().equals("]")){
                    ErrDump.error_k(CurLine);
                    backTok(1);
                }

                judTok = getTok();
            } while (judTok.getVal().equals("["));
            isArray = true;
        }

        if(judTok.getVal().equals("=")){
            InitValAST initValAST = parseInitValAST();
            if(isArray){
                return new VarDefAST(ident, initValAST, constExpASTS, line);
            }
            else return new VarDefAST(ident, initValAST, line);
        }

        backTok(1);

        if(isArray){
            return new VarDefAST(ident, constExpASTS, line);
        }
        else return new VarDefAST(ident, line);
    }

    private InitValAST parseInitValAST() throws IOException {
        Token judTok = getTok();
        if(judTok.getVal().equals("{")){
            ArrayList<InitValAST> initValASTS = new ArrayList<>();

            judTok = getTok();
            if(!judTok.getVal().equals("}")){
                backTok(1);
                InitValAST initValAST = parseInitValAST();
                initValASTS.add(initValAST);
                while (true){
                    judTok = getTok();
                    if(judTok.getVal().equals("}")) break;
                    else {
                        initValAST = parseInitValAST();
                        initValASTS.add(initValAST);
                    }
                }
            }

            return new InitValAST(initValASTS);
        }
        else {
            backTok(1);
            ExpAST expAST = parseExpAST();
            return new InitValAST(expAST);
        }
    }

    private ConstDeclAST parseConstDeclAST() throws IOException {
        Token constTok = getTok();   //  Consume 'const'
        getTok();   //  Consume BType 'int'

        ConstDeclAST constDeclAST = new ConstDeclAST();

        ConstDefAST constDefAST = parseConstDefAST();
        constDeclAST.addConstDef(constDefAST);

        while (true) {
            Token judTok = getTok();
            if(judTok.getVal().equals(";")){
                break;
            }
            //  错误处理i
            if(!judTok.getVal().equals(",")){
                backTok(1);
                ErrDump.error_i(CurLine);
                break;
            }
            //  这里不用backTok的原因是如果不是';', 那就一定是','. 而','我们也应去除。
            constDefAST = parseConstDefAST();
            constDeclAST.addConstDef(constDefAST);
        }

        return constDeclAST;
    }

    private ConstDefAST parseConstDefAST() throws IOException {
        Token identTok = getTok();
        String ident = identTok.getVal();
        int line = identTok.getLine();
        ArrayList<ConstExpAST> constExpASTS = new ArrayList<>();

        Token judTok = getTok();
        if(judTok.getVal().equals("[")){
            while(true){
                ConstExpAST constExpAST = parseConstExpAST();
                constExpASTS.add(constExpAST);

                judTok = getTok();   //  Consume ']'
                if(!judTok.getVal().equals("]")){
                    backTok(1);
                    ErrDump.error_k(CurLine);
                }


                judTok = getTok();
                if(!judTok.getVal().equals("[")){
                    backTok(1);
                    break;
                }
            }
        }
        else{
            backTok(1);
        }


        getTok();   //  Consume '='

        ConstInitValAST constInitValAST = parseConstInitValAST();

        if(constExpASTS.size() != 0) return new ConstDefAST(ident, constExpASTS, constInitValAST, line);
        else return new ConstDefAST(ident, constInitValAST, line);
    }

    private ConstExpAST parseConstExpAST() throws IOException {
        AddExpAST addExpAST = parseAddExpAST();
        return new ConstExpAST(addExpAST);
    }

    private ConstInitValAST parseConstInitValAST() throws IOException {
        Token judTok = getTok();
        if(judTok.getVal().equals("{")){
            ArrayList<ConstInitValAST> constInitValASTS = new ArrayList<>();
            do {
                ConstInitValAST constInitValAST = parseConstInitValAST();
                constInitValASTS.add(constInitValAST);

                judTok = getTok();
            } while (!judTok.getVal().equals("}"));
            return new ConstInitValAST(constInitValASTS);
        }
        else {
            backTok(1);
            ConstExpAST constExpAST = parseConstExpAST();
            return new ConstInitValAST(constExpAST);
        }
    }

    private FuncFParamAST parseFuncFParamAST() throws IOException {
        String bType = getTok().getVal();
        Token identTok = getTok();
        String ident = identTok.getVal();
        int line = identTok.getLine();

        Token judTok = getTok();
        if(judTok.getVal().equals("[")){
            judTok = getTok();   //  Consume ']'
            if(!judTok.getVal().equals("]")){
                backTok(1);
                ErrDump.error_k(CurLine);
            }

            ArrayList<ConstExpAST> constExpASTS = new ArrayList<>();

            while (true){
                judTok = getTok();
                if(judTok.getVal().equals("[")){
                    ConstExpAST constExpAST = parseConstExpAST();
                    constExpASTS.add(constExpAST);

                    judTok = getTok();   //  Consume ']'
                    if(!judTok.getVal().equals("]")){
                        backTok(1);
                        ErrDump.error_k(CurLine);
                    }
                }
                else {
                    backTok(1);
                    break;
                }
            }
            return new FuncFParamAST(bType, ident, constExpASTS);
        }
        else {
            backTok(1);
            return new FuncFParamAST(bType, ident);
        }
    }

    private FuncFParamsAST parseFuncFParamsAST() throws IOException {
        ArrayList<FuncFParamAST> funcFParamASTS = new ArrayList<>();

        FuncFParamAST funcFParamAST = parseFuncFParamAST();
        funcFParamASTS.add(funcFParamAST);

        while(true){
            Token judTok = getTok();
            if(judTok.getVal().equals(",")){
                funcFParamAST = parseFuncFParamAST();
                funcFParamASTS.add(funcFParamAST);
            }
            else {
                backTok(1);
                break;
            }
        }

        return new FuncFParamsAST(funcFParamASTS);
    }

    private FuncDefAST parseFuncDefAST() throws IOException {
        FuncDefAST funcDefAST;
        BlockAST blockAST;
        Token funcTypeToken = getTok();
        String funcType = funcTypeToken.getVal();

        Token identToken = getTok();
        String ident = identToken.getVal();
        int line = identToken.getLine();

        getTok();   //  Consume '('

        FuncFParamsAST funcFParams;

        Token judTok = getTok();
        if(!judTok.getVal().equals(")")){
            backTok(1);

            //  先判断一下是不是没有参数同时缺右括号的错误处理
            if(judTok.getType() != Tokens.INTTK){
                ErrDump.error_j(CurLine);
                blockAST = parseBlockAST();
                funcDefAST = new FuncDefAST(funcType, ident, blockAST, line);
            }
            else {
                funcFParams = parseFuncFParamsAST();

                judTok = getTok();   //  Consume ')'
                if (!judTok.getVal().equals(")")) {
                    backTok(1);
                    ErrDump.error_j(CurLine);
                }

                blockAST = parseBlockAST();
                funcDefAST = new FuncDefAST(funcType, ident, blockAST, funcFParams, line);
            }
        }
        else{
            blockAST = parseBlockAST();
            funcDefAST = new FuncDefAST(funcType, ident, blockAST, line);
        }

        if(funcType.equals("void")) ErrDump.error_f(blockAST);
        else ErrDump.error_g(blockAST);

        return funcDefAST;
    }

    public CompUnitAST parseCompUnitAST() throws IOException {
        ArrayList<DeclAST> declASTS = new ArrayList<>();
        while(true){
            Token judTok = getTok();
            if(judTok.getVal().equals("const")){
                backTok(1);
                DeclAST declAST = parseDeclAST();
                declASTS.add(declAST);
            }
            else if(judTok.getVal().equals("int")){
                getTok();   //  Consume 'ident'
                judTok = getTok();
                backTok(3);
                if(judTok.getVal().equals("(")){    //  FuncDef这里一定是"("
                    break;
                }
                else{
                    DeclAST declAST = parseDeclAST();
                    declASTS.add(declAST);
                }
            }
            else{
                backTok(1);
                break;
            }
        }


        ArrayList<FuncDefAST> funcDefASTS = new ArrayList<>();
        FuncDefAST funcDefAST = parseFuncDefAST();
        funcDefASTS.add(funcDefAST);
        while(true){
            Token judTok = getTok();
            if(judTok == null){
                break;
            }
            else {
                backTok(1);
                funcDefAST = parseFuncDefAST();
                funcDefASTS.add(funcDefAST);
            }
        }

        return new CompUnitAST(declASTS, funcDefASTS);
    }

}
