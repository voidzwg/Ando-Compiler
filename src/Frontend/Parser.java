package Frontend;

import Frontend.AST.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Parser {
    private final String inputFile;
    private final BufferedWriter out;
    private final Lexer lexer;

    private Token getTok() throws IOException {
        return this.lexer.getTok();
    }

    //  Constructor
    public Parser(String inputFile, String outputFile) throws IOException {
        this.inputFile = inputFile;
        this.out = new BufferedWriter(new FileWriter(outputFile));
        this.lexer = new Lexer(inputFile);
    }

    //  Parse Methods
    private NumberAST parseNumberAST() throws IOException {
        Token numberToken = getTok();
        int intConst = Integer.parseInt(numberToken.getVal());

        out.write(numberToken.getType().toString() + " " + numberToken.getVal() + '\n');
        //  Dump
        out.write("<Number>\n");

        return new NumberAST(intConst);
    }


    private StmtAST parseStmtAST() throws IOException {
        Token retToken = getTok();   //  Consume 'return'
        out.write(retToken.getType().toString() + " return\n");

        NumberAST numberAST = parseNumberAST();
        Token semcol = getTok();   //  Consume ';'
        out.write(semcol.getType().toString() + " ;\n");

        //  Dump
        out.write("<Stmt>\n");

        return new StmtAST(numberAST);
    }

    private BlockAST parseBlockAST() throws IOException {
        Token lBrace = getTok();   //  Consume '{'
        out.write(lBrace.getType().toString() + " {\n");

        StmtAST stmtAST = parseStmtAST();

        Token rBrace = getTok();   //  Consume '}'
        out.write(rBrace.getType().toString() + " }\n");

        //  Dump
        out.write("<Block>\n");

        return new BlockAST(stmtAST);
    }

    private FuncDefAST parseFuncDefAST() throws IOException {
        Token funcTypeToken = getTok();
        String funcType = funcTypeToken.getVal();

        Token identToken = getTok();
        String ident = identToken.getVal();

        Token lBraket = getTok();   //  Consume '('
        Token rBraket = getTok();   //  Consume ')'


        out.write(funcTypeToken.getType().toString() + " " + funcType + '\n');
        out.write(identToken.getType().toString() + " " + ident + '\n');
        out.write(lBraket.getType().toString() + " (\n");
        out.write(rBraket.getType().toString() + " )\n");

        BlockAST blockAST = parseBlockAST();

        //  Dump
        if(ident.equals("main")){
            out.write("<MainFuncDef>\n");
        }
        else out.write("<FuncDef>\n");

        return new FuncDefAST(funcType, ident, blockAST);
    }

    public CompUnitAST parseCompUnitAST() throws IOException {

        FuncDefAST funcDefAST = parseFuncDefAST();

        //  Dump
        out.write("<CompUnit>\n");
        out.close();

        return new CompUnitAST(funcDefAST);
    }

}
