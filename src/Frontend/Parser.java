package Frontend;

import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/*
* Parser中变量简介:
* isBack: 用来标记是否有parser函数想要退回上一个Token, 使用于backTok中，是我认为很巧妙的一个思路
* CurTok: 用来存储当前访问到的Tok
* judTok: 函数中命名的judTok一般用于一个非终结符可能有多种方案构成时，用一个judTok来进行判断
*
* */

public class Parser {

    private ArrayList<Token> CurTok = new ArrayList<>();
    private int cTop = -1;
    private final Lexer lexer;

    private Token getTok() throws IOException {
        if(cTop == CurTok.size() - 1){
            CurTok.add(lexer.getTok());
        }
        cTop++;
        return CurTok.get(cTop);
    }

    private void backTok(int index){
        cTop-=index;
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

        //  "return" Exp ";"
        if(judTok.getVal().equals("return")) {
            ExpAST expAST = parseExpAST();
            getTok();   //  Consume ';'
            return new StmtAST(expAST);
        }

        //  LVal "=" Exp ";"
        else if(judTok.getType() == Tokens.IDENFR){
            backTok(1);
            LValAST lValAST = parseLValAST();
            getTok();   //  Consume '='
            ExpAST expAST = parseExpAST();
            getTok();   //  Consume ';'
            return new StmtAST(lValAST, expAST);
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
            getTok();   //  Consume ')'
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
            getTok();   //  Consume '('
            CondAST condAST = parseCondAST();
            getTok();   //  Consume ')'
            StmtAST loopStmt = parseStmtAST();
            return new StmtAST(condAST, loopStmt);
        }

        else if(judTok.getVal().equals("continue")){
            getTok();   //  Consume ';'
            ContinueAST continueAST = new ContinueAST();
            return new StmtAST(continueAST);
        }

        else if(judTok.getVal().equals("break")){
            getTok();   //  Consume ';'
            BreakAST breakAST = new BreakAST();
            return new StmtAST(breakAST);
        }

        //  [Exp] ;
        else {
            if(!judTok.getVal().equals(";")) {
                backTok(1);
                ExpAST expAST = parseExpAST();
                return new StmtAST(expAST, true);
            }
            else return new StmtAST(null, false);
        }
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
        switch (judTok.getVal()) {
            case "*" -> {
                MulExpAST mulExpAST = parseMulExpAST();
                return new MulExpAST(unaryExpAST, "*", mulExpAST);
            }
            case "/" -> {
                MulExpAST mulExpAST = parseMulExpAST();
                return new MulExpAST(unaryExpAST, "/", mulExpAST);
            }
            case "%" -> {
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
            return new UnaryExpAST(judTok.getVal(), unaryExpAST);
        }
        else if(judTok.getType() == Tokens.IDENFR){
            judTok = getTok();
            if(judTok.getVal().equals("(")){
                backTok(2);
                String ident = getTok().getVal();
                FuncRParamsAST funcRParamsAST = parseFuncRParamsAST();
                return new UnaryExpAST(ident, funcRParamsAST);
            }
            else {
                backTok(2);
                PrimaryExpAST primaryExpAST = parsePrimaryExpAST();
                return new UnaryExpAST(primaryExpAST);
            }
        }
        //  不是ident肯定是PrimaryExp的情况
        else {
            backTok(1);
            PrimaryExpAST primaryExpAST = parsePrimaryExpAST();
            return new UnaryExpAST(primaryExpAST);
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
        String ident = getTok().getVal();
        return new LValAST(ident);
    }

    private BlockAST parseBlockAST() throws IOException {
        getTok();   //  Consume '{'

        BlockAST blockAST = new BlockAST();

        while (!getTok().getVal().equals("}")) {
            backTok(1);
            BlockItemAST blockItemAST = parseBlockItemAST();
            blockAST.addBlockItem(blockItemAST);
        }

        return blockAST;
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
        getTok();   //  Consume BType 'int'

        VarDeclAST varDeclAST = new VarDeclAST();

        VarDefAST varDefAST = parseVarDefAST();
        varDeclAST.addVarDef(varDefAST);

        while (true){
            Token judTok = getTok();
            if(judTok.getVal().equals(";")){
                break;
            }
            //  这里不用backTok的原因是如果不是';', 那就一定是','. 而','我们也应去除。
            varDefAST = parseVarDefAST();
            varDeclAST.addVarDef(varDefAST);
        }

        return varDeclAST;
    }

    private VarDefAST parseVarDefAST() throws IOException {
        String ident = getTok().getVal();

        Token judTok = getTok();
        if(judTok.getVal().equals("=")){
            InitValAST initValAST = parseInitValAST();
            return new VarDefAST(ident, initValAST);
        }

        backTok(1);
        return new VarDefAST(ident);
    }

    private InitValAST parseInitValAST() throws IOException {
        ExpAST expAST = parseExpAST();
        return new InitValAST(expAST);
    }

    private ConstDeclAST parseConstDeclAST() throws IOException {
        getTok();   //  Consume 'const'
        getTok();   //  Consume BType 'int'

        ConstDeclAST constDeclAST = new ConstDeclAST();

        ConstDefAST constDefAST = parseConstDefAST();
        constDeclAST.addConstDef(constDefAST);

        while (true) {
            Token judTok = getTok();
            if(judTok.getVal().equals(";")){
                break;
            }
            //  这里不用backTok的原因是如果不是';', 那就一定是','. 而','我们也应去除。
            constDefAST = parseConstDefAST();
            constDeclAST.addConstDef(constDefAST);
        }

        return constDeclAST;
    }

    private ConstDefAST parseConstDefAST() throws IOException {
        String ident = getTok().getVal();

        getTok();   //  Consume '='

        ConstInitValAST constInitValAST = parseConstInitValAST();

        return new ConstDefAST(ident, constInitValAST);
    }

    private ConstExpAST parseConstExpAST() throws IOException {
        AddExpAST addExpAST = parseAddExpAST();
        return new ConstExpAST(addExpAST);
    }

    private ConstInitValAST parseConstInitValAST() throws IOException {

        ConstExpAST constExpAST = parseConstExpAST();
        return new ConstInitValAST(constExpAST);
    }

    private FuncFParamAST parseFuncFParamAST() throws IOException {
        String bType = getTok().getVal();
        String ident = getTok().getVal();

        return new FuncFParamAST(bType, ident);
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
        Token funcTypeToken = getTok();
        String funcType = funcTypeToken.getVal();

        Token identToken = getTok();
        String ident = identToken.getVal();

        getTok();   //  Consume '('

        FuncFParamsAST funcFParams = null;

        Token judTok = getTok();
        if(!judTok.getVal().equals(")")){
            funcFParams = parseFuncFParamsAST();
        }

        BlockAST blockAST = parseBlockAST();

        return new FuncDefAST(funcType, ident, blockAST, funcFParams);
    }

    public CompUnitAST parseCompUnitAST() throws IOException {
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

        return new CompUnitAST(funcDefASTS);
    }

}
