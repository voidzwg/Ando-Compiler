package Utils;

import Frontend.AST.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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

        DumpStmtAST(blockAST.getStmtAST());

        out.write("RBRACE }\n");
        out.write("<Block>\n");
    }

    private static void DumpStmtAST(StmtAST stmtAST) throws IOException {
        out.write("RETURNTK return\n");

        DumpExpAST(stmtAST.getExpAST());

        out.write("SEMICN ;\n");
        out.write("<Stmt>\n");
    }

    private static void DumpExpAST(ExpAST expAST) throws IOException {
        DumpUnaryExpAST(expAST.getUnaryExp());

        out.write("<Exp>\n");
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
        else DumpNumberAST(primaryExpAST.getNumberAST());
        out.write("<PrimaryExp>\n");
    }

    private static void DumpNumberAST(NumberAST numberAST) throws IOException {
        out.write("INTCON " + numberAST.getIntConst() + "\n");


        out.write("<Number>\n");
    }
}
