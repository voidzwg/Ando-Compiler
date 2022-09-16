package Utils;

import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
import Frontend.AST.ExpAST.*;
import Frontend.Tokens;

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
        ArrayList<FuncDefAST> funcDefASTS = compUnitAST.getFuncDefASTS();
        int len = funcDefASTS.size();

        for (FuncDefAST funcDefAST : funcDefASTS) {
            DumpFuncDefAST(funcDefAST);
        }

        out.write("<CompUnit>");
        out.close();
    }

    private static void DumpFuncFParamsAST(FuncFParamsAST funcFParamsAST) throws IOException {
        ArrayList<FuncFParamAST> funcFParamASTS = funcFParamsAST.getFuncFParamASTS();

        for(FuncFParamAST funcFParamAST : funcFParamASTS){
            DumpFuncFParamAST(funcFParamAST);
        }

        out.write("<FuncFParams>");
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

        if(funcDefAST.getType() == 2){

            FuncFParamsAST funcFParamsAST = funcDefAST.getFuncFParamsAST();
            DumpFuncFParamsAST(funcFParamsAST);
        }

        out.write("RPARENT )\n");

        DumpBlockAST(funcDefAST.getBlockAST());

        if(funcName.equals("main")) out.write("<MainFuncDef>\n");
        else out.write("<FuncDefUnit>\n");
    }

    private static void DumpFuncFParamAST(FuncFParamAST funcFParamAST) throws IOException {
        String bType = funcFParamAST.getbType();
        if(bType.equals("void")) out.write("VOIDTK void\n");
        else if(bType.equals("int")) out.write("INTTK int\n");

        String ident = funcFParamAST.getIdent();
        out.write("IDENFR " + ident + "\n");

        out.write("<FuncFParam\n>");
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
        if(declAST.getType() == 1) {
            DumpConstDeclAST(declAST.getConstDeclAST());
        }
        else DumpVarDeclAST(declAST.getVarDeclAST());

        out.write("<Decl>\n");
    }

    private static void DumpVarDeclAST(VarDeclAST varDeclAST) throws IOException {
        out.write("INTTK int\n");
        ArrayList<VarDefAST> varDefASTS = varDeclAST.getVarDefASTS();
        for(int i = 0; i < varDefASTS.size(); i++){
            DumpVarDefAST(varDefASTS.get(i));
            if(i != varDefASTS.size() - 1) out.write("COMMA ,\n");
        }

        out.write("SEMICN ;\n");
        out.write("<VarDecl>\n");
    }

    private static void DumpVarDefAST(VarDefAST varDefAST) throws IOException {
        out.write("IDENFR " + varDefAST.getIdent() + "\n");

        if(varDefAST.getType() == 2){
            out.write("ASSIGN =\n");
            DumpInitValAST(varDefAST.getInitValAST());
        }

        out.write("<VarDefAST>\n");
    }

    private static void DumpInitValAST(InitValAST initValAST) throws IOException {
        DumpExpAST(initValAST.getExpAST());

        out.write("<InitValAST>\n");
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
        if(stmtAST.getType() == 1) {
            out.write("RETURNTK return\n");
            DumpExpAST(stmtAST.getExpAST());
            out.write("SEMICN ;\n");
        }
        else if(stmtAST.getType() == 2){
            DumpLValAST(stmtAST.getLValAST());
            out.write("ASSIGN =\n");
            DumpExpAST(stmtAST.getExpAST());
            out.write("SEMICN ;\n");
        }
        else if(stmtAST.getType() == 3){
            DumpBlockAST(stmtAST.getBlockAST());
        }
        else if(stmtAST.getType() == 4){
            if(stmtAST.isHasExp()){
                DumpExpAST(stmtAST.getExpAST());
            }
            out.write("SEMICN ;\n");
        }
        else if(stmtAST.getType() == 5){
            out.write("IFTK if\n");
            out.write("LBRACK (\n");
            DumpCondAST(stmtAST.getCondAST());
            out.write("RBRACK )\n");
            DumpStmtAST(stmtAST.getIfStmtAST());
        }
        else if(stmtAST.getType() == 6){
            out.write("IFTK if\n");
            out.write("LBRACK (\n");
            DumpCondAST(stmtAST.getCondAST());
            out.write("RBRACK )\n");
            DumpStmtAST(stmtAST.getIfStmtAST());
            out.write("ELSETK else\n");
            DumpStmtAST(stmtAST.getElseStmtAST());
        }
        else if(stmtAST.getType() == 7){
            out.write("WHILETK while\n");
            out.write("LBRACK (\n");
            DumpCondAST(stmtAST.getCondAST());
            out.write("RBRACK )\n");

            DumpStmtAST(stmtAST.getLoopStmt());
        }
        else if(stmtAST.getType() == 8){
            out.write("BREAKTK break\n");
            out.write("SEMCOL ;\n");
        }
        else if(stmtAST.getType() == 9){
            out.write("CONTINUETK continue\n");
            out.write("SEMCOL ;\n");
        }

        out.write("<Stmt>\n");
    }

    private static void DumpCondAST(CondAST condAST) throws IOException {
        DumpLOrExpAST(condAST.getLOrExpAST());
        out.write("<Cond>\n");
    }

    private static void DumpLOrExpAST(LOrExpAST lOrExpAST) throws IOException {
        if(lOrExpAST.getType() == 1){
            DumpLAndExpAST(lOrExpAST.getLAndExpAST());
        }
        else if(lOrExpAST.getType() == 2){
            DumpLAndExpAST(lOrExpAST.getLAndExpAST());
            out.write("OR ||\n");
            DumpLOrExpAST(lOrExpAST.getLOrExpAST());
        }

        out.write("<LOrExp>\n");
    }

    private static void DumpLAndExpAST(LAndExpAST lAndExpAST) throws IOException {
        DumpEqExpAST(lAndExpAST.getEqExpAST());
        if(lAndExpAST.getType() == 2){
            out.write("AND &&\n");
            DumpLAndExpAST(lAndExpAST.getLAndExpAST());
        }
        out.write("<LAndExp>\n");
    }

    private static void DumpEqExpAST(EqExpAST eqExpAST) throws IOException {
        DumpRelExpAST(eqExpAST.getRelExpAST());
        if(eqExpAST.getType() == 2) {
            String op = eqExpAST.getOp();
            if (op.equals("==")) out.write("EQL ==\n");
            else out.write("NEQ !=\n");
            DumpEqExpAST(eqExpAST.getEqExpAST());
        }

        out.write("<EqExp>\n");
    }

    private static void DumpRelExpAST(RelExpAST relExpAST) throws IOException {
        DumpAddExpAST(relExpAST.getAddExpAST());
        if(relExpAST.getType() == 2){
            String op = relExpAST.getOp();
            switch (op) {
                case "<" -> out.write("LSS <\n");
                case "<=" -> out.write("LEQ <=\n");
                case ">" -> out.write("GRE >\n");
                case ">=" -> out.write("GEQ >=\n");
            }
            DumpRelExpAST(relExpAST.getRelExpAST());
        }
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
