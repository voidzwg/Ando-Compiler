package Frontend;

import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;
import Utils.Global;

import java.io.IOException;

/*
* Parser中变量简介:
* isBack: 用来标记是否有parser函数想要退回上一个Token, 使用于backTok中，是我认为很巧妙的一个思路
* CurTok: 用来存储当前访问到的Tok
* judTok: 函数中命名的judTok一般用于一个非终结符可能有多种方案构成时，用一个judTok来进行判断
*
* */

public class Parser {

    private boolean isBack = false;
    private Token CurTok;
    private final Lexer lexer;

    private Token getTok() throws IOException {
        if(!isBack){
            CurTok = lexer.getTok();
        }
        else {
            isBack = false;
        }
        return CurTok;
    }

    private void backTok(){
        isBack = true;
    }

    //  Constructor
    public Parser() throws IOException {
        String inputFile = Global.inputFile;
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
        backTok();
        LValAST lValAST = parseLValAST();
        getTok();   //  Consume '='
        ExpAST expAST = parseExpAST();
        getTok();   //  Consume ';'

        return new StmtAST(lValAST, expAST);
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

        backTok();
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

        backTok();
        return new MulExpAST(unaryExpAST);

    }

    private UnaryExpAST parseUnaryExpAST() throws IOException {
        Token judTok = getTok();

        //  UnaryOP UnaryExp的情况
        if(judTok.getVal().equals("+") || judTok.getVal().equals("-") || judTok.getVal().equals("!")){
            UnaryExpAST unaryExpAST = parseUnaryExpAST();
            return new UnaryExpAST(judTok.getVal(), unaryExpAST);
        }

        //  PrimaryExp的情况
        backTok();
        PrimaryExpAST primaryExpAST = parsePrimaryExpAST();

        return new UnaryExpAST(primaryExpAST);
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
            backTok();
            LValAST lValAST = parseLValAST();

            return new PrimaryExpAST(lValAST);
        }

        //  Number
        backTok();
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
            backTok();
            BlockItemAST blockItemAST = parseBlockItemAST();
            blockAST.addBlockItem(blockItemAST);
        }

        return blockAST;
    }

    private BlockItemAST parseBlockItemAST() throws IOException {
        Token judTok = getTok();
        backTok();
        if(judTok.getVal().equals("const")){
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
        backTok();
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

        backTok();
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



    private FuncDefAST parseFuncDefAST() throws IOException {
        Token funcTypeToken = getTok();
        String funcType = funcTypeToken.getVal();

        Token identToken = getTok();
        String ident = identToken.getVal();

        getTok();   //  Consume '('
        getTok();   //  Consume ')'

        BlockAST blockAST = parseBlockAST();

        return new FuncDefAST(funcType, ident, blockAST);
    }

    public CompUnitAST parseCompUnitAST() throws IOException {

        FuncDefAST funcDefAST = parseFuncDefAST();
        return new CompUnitAST(funcDefAST);
    }

}
