package Utils;

import Frontend.AST.*;
import Frontend.AST.DeclAST.ConstDeclAST;
import Frontend.AST.DeclAST.ConstDefAST;
import Frontend.AST.DeclAST.ConstInitValAST;
import Frontend.AST.DeclAST.DeclAST;
import Frontend.AST.ExpAST.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ASTDump {
    private static final BufferedWriter out;

    static {
        try {
            out = new BufferedWriter(new FileWriter(Global.outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void DumpCompUnit(CompUnitAST compUnitAST) throws IOException {
        DumpFuncDefAST(compUnitAST.getFuncDefAST());
        out.write("<CompUnit>");
        out.close();
    }

    private static void DumpFuncDefAST(FuncDefAST funcDefAST) throws IOException {
        if(funcDefAST.getFuncType().equals("int")){
            out.write("INTTK int\n");
        }
        else out.write("VOIDTK void\n");

        String funcName = funcDefAST.getIdent();
        if(funcName.equals("main")) out.write("MAINTK main\n");
        else out.write("IDENFR " + funcName + "\n");

        out.write("LPARENT (\n");
        out.write("RPARENT )\n");

        DumpBlockAST(funcDefAST.getBlockAST());

        if(funcName.equals("main")) out.write("<MainFuncDef>\n");
        else out.write("<FuncDefUnit>\n");
    }

    private static void DumpBlockAST(BlockAST blockAST) throws IOException {
        out.write("LBRACE {\n");

        ArrayList<BlockItemAST> blockItemASTS = blockAST.getBlockItems();
        for (BlockItemAST blockItemAST : blockItemASTS) {
            DumpBlockItemAST(blockItemAST);
        }

        out.write("RBRACE }\n");
        out.write("<Block>\n");
    }

    private static void DumpBlockItemAST(BlockItemAST blockItemAST) throws IOException {
        if(blockItemAST.getType() == 1){
            DumpDeclAST(blockItemAST.getDeclAST());
        }
        else if(blockItemAST.getType() == 2){
            DumpStmtAST(blockItemAST.getStmtAST());
        }

        out.write("<BlockItem>\n");
    }

    private static void DumpDeclAST(DeclAST declAST) throws IOException {
        DumpConstDeclAST(declAST.getConstDeclAST());

        out.write("<Decl>\n");
    }

    private static void DumpConstDeclAST(ConstDeclAST constDeclAST) throws IOException {
        out.write("CONSTTK const\n");
        out.write("INTTK int\n");

        ArrayList<ConstDefAST> constDefASTS = constDeclAST.getConstDefASTS();
        for(int i = 0; i < constDefASTS.size(); i++){
            DumpConstDefAST(constDefASTS.get(i));

            if(i != constDefASTS.size() - 1){
                out.write("COMMA ,\n");
            }
        }

        out.write("SEMICN ;\n");

        out.write("<ConstDecl>\n");
    }

    private static void DumpConstDefAST(ConstDefAST constDefAST) throws IOException {
        out.write("IDENFR " + constDefAST.getIdent() + "\n");
        out.write("ASSIGN =\n");

        DumpConstInitValAST(constDefAST.getConstInitValAST());

        out.write("<ConstDef>\n");
    }

    private static void DumpConstInitValAST(ConstInitValAST constInitValAST) throws IOException {
        DumpConstExpAST(constInitValAST.getConstExpAST());

        out.write("<ConstInitVal>\n");
    }

    private static void DumpConstExpAST(ConstExpAST constExpAST) throws IOException {
        DumpAddExpAST(constExpAST.getAddExpAST());

        out.write("<ConstExp>\n");
    }

    private static void DumpStmtAST(StmtAST stmtAST) throws IOException {
        out.write("RETURNTK return\n");

        DumpExpAST(stmtAST.getExpAST());

        out.write("SEMICN ;\n");
        out.write("<Stmt>\n");
    }

    private static void DumpExpAST(ExpAST expAST) throws IOException {
        DumpAddExpAST(expAST.getAddExpAST());

        out.write("<Exp>\n");
    }

    private static void DumpAddExpAST(AddExpAST addExpAST) throws IOException {
        DumpMulExpAST(addExpAST.getMulExpAST());
        if (addExpAST.getType() != 1) {
            String op = addExpAST.getOp();
            if (op.equals("+")) {
                out.write("PLUS +\n");
            } else if (op.equals("-")) out.write("MINU -\n");
            DumpAddExpAST(addExpAST.getAddExpAST());
        }

        out.write("<AddExp>\n");
    }

    private static void DumpMulExpAST(MulExpAST mulExpAST) throws IOException {
        DumpUnaryExpAST(mulExpAST.getUnaryExpAST());
        if(mulExpAST.getType() != 1){
            String op = mulExpAST.getOp();
            switch (op) {
                case "*" -> out.write("MULT *\n");
                case "/" -> out.write("DIV /");
                case "%" -> out.write("MOD %");
            }
            DumpMulExpAST(mulExpAST.getMulExpAST());
        }

        out.write("<MulExp>\n");
    }

    private static void DumpUnaryExpAST(UnaryExpAST unaryExpAST) throws IOException {
        //  PrimaryExp
        if(unaryExpAST.getType() == 1){
            DumpPrimaryExp(unaryExpAST.getPrimaryExpAST());
        }

        else{
            String op = unaryExpAST.getUnaryOP();
            switch (op) {
                case "+" -> {
                    out.write("PLUS +\n");
                    out.write("<UnaryOp>\n");
                }
                case "-" -> {
                    out.write("MINU -\n");
                    out.write("<UnaryOp>\n");
                }
                case "!" -> {
                    out.write("NOT !\n");
                    out.write("<UnaryOp>\n");
                }
            }

            DumpUnaryExpAST(unaryExpAST.getUnaryExpAST());
        }

        out.write("<UnaryExp>\n");
    }

    private static void DumpPrimaryExp(PrimaryExpAST primaryExpAST) throws IOException {
        //  "(" Exp ")"
        if(primaryExpAST.getType() == 1) {
            out.write("LBRACK (\n");

            DumpExpAST(primaryExpAST.getExpAST());

            out.write("RBRACK )\n");
        }

        //  Number
        else if(primaryExpAST.getType() == 2){
            DumpNumberAST(primaryExpAST.getNumberAST());
        }

        //  LVal
        else if(primaryExpAST.getType() == 3){
            DumpLValAST(primaryExpAST.getlValAST());
        }

        out.write("<PrimaryExp>\n");
    }

    private static void DumpLValAST(LValAST lValAST) throws IOException {
        out.write("IDENFR " + lValAST.getIdent() + "\n");

        out.write("<LVal>\n");
    }

    private static void DumpNumberAST(NumberAST numberAST) throws IOException {
        out.write("INTCON " + numberAST.getIntConst() + "\n");


        out.write("<Number>\n");
    }
}
