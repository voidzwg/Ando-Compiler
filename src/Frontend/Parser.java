package Frontend;

import Frontend.AST.*;
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
        this.lexer = new Lexer(inputFile);
    }

    //  Parse Methods
    private NumberAST parseNumberAST() throws IOException {
        Token numberToken = getTok();
        int intConst = Integer.parseInt(numberToken.getVal());

        return new NumberAST(intConst);
    }


    private StmtAST parseStmtAST() throws IOException {
        getTok();   //  Consume 'return'

        ExpAST expAST = parseExpAST();
        getTok();   //  Consume ';'

        return new StmtAST(expAST);
    }

    private ExpAST parseExpAST() throws IOException {
        UnaryExpAST unaryExpAST = parseUnaryExpAST();

        return new ExpAST(unaryExpAST);
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

        //  Number
        backTok();
        NumberAST numberAST = parseNumberAST();

        return new PrimaryExpAST(numberAST);
    }

    private BlockAST parseBlockAST() throws IOException {
        getTok();   //  Consume '{'

        StmtAST stmtAST = parseStmtAST();

        getTok();   //  Consume '}'

        return new BlockAST(stmtAST);
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
