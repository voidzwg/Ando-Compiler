package Utils;

import Frontend.AST.*;
import Frontend.AST.DeclAST.*;
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
        ArrayList<DeclAST> declASTS = compUnitAST.getDeclASTS();
        for(DeclAST declAST : declASTS){
            DumpDeclAST(declAST);
        }

        ArrayList<FuncDefAST> funcDefASTS = compUnitAST.getFuncDefASTS();

        for (FuncDefAST funcDefAST : funcDefASTS) {
            DumpFuncDefAST(funcDefAST);
        }

        out.write("<CompUnit>");
        out.close();
    }

    private static void DumpFuncFParamsAST(FuncFParamsAST funcFParamsAST) throws IOException {
        ArrayList<FuncFParamAST> funcFParamASTS = funcFParamsAST.getFuncFParamASTS();

        for(int i = 0; i < funcFParamASTS.size(); i++){
            DumpFuncFParamAST(funcFParamASTS.get(i));
            if(i != funcFParamASTS.size() - 1) out.write("COMMA ,\n");
        }

        out.write("<FuncFParams>\n");
    }

    private static void DumpFuncDefAST(FuncDefAST funcDefAST) throws IOException {
        if(funcDefAST.getFuncType().equals("int")){
            out.write("INTTK int\n");
        }
        else out.write("VOIDTK void\n");

        String funcName = funcDefAST.getIdent();

        if(!funcName.equals("main")){
            out.write("<FuncType>\n");
        }

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
        else out.write("<FuncDef>\n");
    }

    private static void DumpFuncFParamAST(FuncFParamAST funcFParamAST) throws IOException {
        String bType = funcFParamAST.getbType();
        if(bType.equals("void")) out.write("VOIDTK void\n");
        else if(bType.equals("int")) out.write("INTTK int\n");

        String ident = funcFParamAST.getIdent();
        out.write("IDENFR " + ident + "\n");

        if(funcFParamAST.getType() == 2){
            out.write("LBRACK [\n");
            out.write("RBRACK ]\n");

            ArrayList<ConstExpAST> constExpASTS = funcFParamAST.getConstExpASTS();
            for (ConstExpAST constExpAST : constExpASTS) {
                out.write("LBRACK [\n");
                DumpConstExpAST(constExpAST);
                out.write("RBRACK ]\n");
            }
        }


        out.write("<FuncFParam>\n");
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
    }

    private static void DumpDeclAST(DeclAST declAST) throws IOException {
        if(declAST.getType() == 1) {
            DumpConstDeclAST(declAST.getConstDeclAST());
        }
        else DumpVarDeclAST(declAST.getVarDeclAST());
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
        int varDefType = varDefAST.getType();

        if(varDefType == 3 || varDefType == 4){
            ArrayList<ConstExpAST> constExpASTS = varDefAST.getConstExpASTS();
            for(ConstExpAST constExpAST : constExpASTS){
                out.write("LBRACK [\n");

                DumpConstExpAST(constExpAST);

                out.write("RBRACK ]\n");
            }
        }

        if(varDefType == 2 || varDefType == 4){
            out.write("ASSIGN =\n");
            DumpInitValAST(varDefAST.getInitValAST());
        }

        out.write("<VarDef>\n");
    }

    private static void DumpInitValAST(InitValAST initValAST) throws IOException {
        if(initValAST.getType() == 1){
            DumpExpAST(initValAST.getExpAST());
        }
        else if(initValAST.getType() == 2){
            out.write("LBRACE {\n");

            ArrayList<InitValAST> initValASTS = initValAST.getInitValASTS();
            for(int i = 0; i < initValASTS.size(); i++){
                InitValAST initValAST1 = initValASTS.get(i);
                DumpInitValAST(initValAST1);
                if(i != initValASTS.size() - 1) out.write("COMMA ,\n");
            }

            out.write("RBRACE }\n");
        }

        out.write("<InitVal>\n");
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

        if(constDefAST.getType() == 2){
            ArrayList<ConstExpAST> constExpASTS = constDefAST.getConstExpASTS();
            for(ConstExpAST constExpAST : constExpASTS){
                out.write("LBRACK [\n");
                DumpConstExpAST(constExpAST);
                out.write("RBRACK ]\n");
            }
        }

        out.write("ASSIGN =\n");

        DumpConstInitValAST(constDefAST.getConstInitValAST());

        out.write("<ConstDef>\n");
    }

    private static void DumpConstInitValAST(ConstInitValAST constInitValAST) throws IOException {
        if(constInitValAST.getType() == 1){
            DumpConstExpAST(constInitValAST.getConstExpAST());
        }
        else if(constInitValAST.getType() == 2){
            out.write("LBRACE {\n");

            ArrayList<ConstInitValAST> constInitValASTS = constInitValAST.getConstInitValASTS();
            for(int i = 0; i < constInitValASTS.size(); i++){
                ConstInitValAST constInitValAST1 = constInitValASTS.get(i);
                DumpConstInitValAST(constInitValAST1);

                if(i != constInitValASTS.size() - 1) out.write("COMMA ,\n");
            }

            out.write("RBRACE }\n");
        }

        out.write("<ConstInitVal>\n");
    }

    private static void DumpConstExpAST(ConstExpAST constExpAST) throws IOException {
        DumpAddExpAST(constExpAST.getAddExpAST());

        out.write("<ConstExp>\n");
    }

    private static void DumpStmtAST(StmtAST stmtAST) throws IOException {
        int stmtType = stmtAST.getType();
        if(stmtType == 1) {
            out.write("RETURNTK return\n");
            DumpExpAST(stmtAST.getExpAST());
            out.write("SEMICN ;\n");
        }
        else if(stmtType == 2){
            DumpLValAST(stmtAST.getLValAST());
            out.write("ASSIGN =\n");
            DumpExpAST(stmtAST.getExpAST());
            out.write("SEMICN ;\n");
        }
        else if(stmtType == 3){
            DumpBlockAST(stmtAST.getBlockAST());
        }
        else if(stmtType == 4){
            if(stmtAST.isHasExp()){
                DumpExpAST(stmtAST.getExpAST());
            }
            out.write("SEMICN ;\n");
        }
        else if(stmtType == 5){
            out.write("IFTK if\n");
            out.write("LPARENT (\n");
            DumpCondAST(stmtAST.getCondAST());
            out.write("RPARENT )\n");
            DumpStmtAST(stmtAST.getIfStmtAST());
        }
        else if(stmtType == 6){
            out.write("IFTK if\n");
            out.write("LPARENT (\n");
            DumpCondAST(stmtAST.getCondAST());
            out.write("RPARENT )\n");
            DumpStmtAST(stmtAST.getIfStmtAST());
            out.write("ELSETK else\n");
            DumpStmtAST(stmtAST.getElseStmtAST());
        }
        else if(stmtType == 7){
            out.write("WHILETK while\n");
            out.write("LPARENT (\n");
            DumpCondAST(stmtAST.getCondAST());
            out.write("RPARENT )\n");

            DumpStmtAST(stmtAST.getLoopStmt());
        }
        else if(stmtType == 8){
            out.write("CONTINUETK continue\n");
            out.write("SEMICN ;\n");
        }
        else if(stmtType == 9){
            out.write("BREAKTK break\n");
            out.write("SEMICN ;\n");
        }
        else if(stmtType == 10){
            DumpLValAST(stmtAST.getLValAST());
            out.write("ASSIGN =\n");
            out.write("GETINTTK getint\n");
            out.write("LPARENT (\n");
            out.write("RPARENT )\n");
            out.write("SEMICN ;\n");
        }
        else if(stmtType == 11){
            out.write("PRINTFTK printf\n");
            out.write("LPARENT (\n");

            out.write("STRCON " + stmtAST.getfString() + "\n");

            ArrayList<ExpAST> expASTS = stmtAST.getExpASTS();
            for(ExpAST expAST : expASTS){
                out.write("COMMA ,\n");
                DumpExpAST(expAST);
            }
            out.write("RPARENT )\n");
            out.write("SEMICN ;\n");
        }
        else if(stmtType == 12){
            out.write("RETURNTK return\n");
            out.write("SEMICN ;\n");
        }

        out.write("<Stmt>\n");
    }

    private static void DumpCondAST(CondAST condAST) throws IOException {
        DumpLOrExpAST(condAST.getLOrExpAST());
        out.write("<Cond>\n");
    }

    private static void DumpLOrExpAST(LOrExpAST lOrExpAST) throws IOException {
        DumpLAndExpAST(lOrExpAST.getLAndExpAST());
        out.write("<LOrExp>\n");
        if(lOrExpAST.getType() == 2){
            out.write("OR ||\n");
            DumpLOrExpAST(lOrExpAST.getLOrExpAST());
        }
    }

    private static void DumpLAndExpAST(LAndExpAST lAndExpAST) throws IOException {
        DumpEqExpAST(lAndExpAST.getEqExpAST());
        out.write("<LAndExp>\n");
        if(lAndExpAST.getType() == 2){
            out.write("AND &&\n");
            DumpLAndExpAST(lAndExpAST.getLAndExpAST());
        }
    }

    private static void DumpEqExpAST(EqExpAST eqExpAST) throws IOException {
        DumpRelExpAST(eqExpAST.getRelExpAST());
        out.write("<EqExp>\n");
        if(eqExpAST.getType() == 2) {
            String op = eqExpAST.getOp();
            if (op.equals("==")) out.write("EQL ==\n");
            else out.write("NEQ !=\n");
            DumpEqExpAST(eqExpAST.getEqExpAST());
        }
    }

    private static void DumpRelExpAST(RelExpAST relExpAST) throws IOException {
        DumpAddExpAST(relExpAST.getAddExpAST());
        out.write("<RelExp>\n");
        if(relExpAST.getType() == 2){
            String op = relExpAST.getOp();
            switch (op) {
                case "<" :{
                    out.write("LSS <\n");
                    break;
                }
                case "<=" :{
                    out.write("LEQ <=\n");
                    break;
                }
                case ">" :{
                    out.write("GRE >\n");
                    break;
                }
                case ">=" :{
                    out.write("GEQ >=\n");
                    break;
                }
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
        out.write("<AddExp>\n");
        if (addExpAST.getType() != 1) {
            String op = addExpAST.getOp();
            if (op.equals("+")) {
                out.write("PLUS +\n");
            } else if (op.equals("-")) out.write("MINU -\n");
            DumpAddExpAST(addExpAST.getAddExpAST());
        }

    }

    private static void DumpMulExpAST(MulExpAST mulExpAST) throws IOException {
        DumpUnaryExpAST(mulExpAST.getUnaryExpAST());
        out.write("<MulExp>\n");
        if(mulExpAST.getType() != 1){
            String op = mulExpAST.getOp();
            switch (op) {
                case "*" :{
                    out.write("MULT *\n");
                    break;
                }
                case "/" :{
                    out.write("DIV /\n");
                    break;
                }
                case "%" :{
                    out.write("MOD %\n");
                    break;
                }
            }
            DumpMulExpAST(mulExpAST.getMulExpAST());
        }
    }

    private static void DumpFuncRParamsAST(FuncRParamsAST funcRParamsAST) throws IOException {
        ArrayList<ExpAST> expASTS = funcRParamsAST.getExpASTS();

        for(int i = 0; i < expASTS.size(); i++){
            DumpExpAST(expASTS.get(i));
            if(i != expASTS.size() - 1) out.write("COMMA ,\n");
        }

        out.write("<FuncRParams>\n");
    }

    private static void DumpUnaryExpAST(UnaryExpAST unaryExpAST) throws IOException {
        //  PrimaryExp
        if(unaryExpAST.getType() == 1){
            DumpPrimaryExp(unaryExpAST.getPrimaryExpAST());
        }

        else if (unaryExpAST.getType() == 2){
            String op = unaryExpAST.getUnaryOP();
            switch (op) {
                case "+" : {
                    out.write("PLUS +\n");
                    out.write("<UnaryOp>\n");
                    break;
                }
                case "-" : {
                    out.write("MINU -\n");
                    out.write("<UnaryOp>\n");
                    break;
                }
                case "!" : {
                    out.write("NOT !\n");
                    out.write("<UnaryOp>\n");
                    break;
                }
            }

            DumpUnaryExpAST(unaryExpAST.getUnaryExpAST());
        }

        else if(unaryExpAST.getType() == 3){
            out.write("IDENFR " + unaryExpAST.getIdent() + "\n");
            out.write("LPARENT (\n");

            DumpFuncRParamsAST(unaryExpAST.getFuncRParamsAST());

            out.write("RPARENT )\n");
        }

        else if(unaryExpAST.getType() == 4){
            out.write("IDENFR " + unaryExpAST.getIdent() + "\n");
            out.write("LPARENT (\n");

            out.write("RPARENT )\n");
        }

        out.write("<UnaryExp>\n");
    }

    private static void DumpPrimaryExp(PrimaryExpAST primaryExpAST) throws IOException {
        //  "(" Exp ")"
        if(primaryExpAST.getType() == 1) {
            out.write("LPARENT (\n");

            DumpExpAST(primaryExpAST.getExpAST());

            out.write("RPARENT )\n");
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

        if(lValAST.getType() == 2){
            ArrayList<ExpAST> expASTS = lValAST.getExpASTS();
            for(ExpAST expAST : expASTS){
                out.write("LBRACK [\n");

                DumpExpAST(expAST);

                out.write("RBRACK ]\n");
            }
        }

        out.write("<LVal>\n");
    }

    private static void DumpNumberAST(NumberAST numberAST) throws IOException {
        out.write("INTCON " + numberAST.getIntConst() + "\n");


        out.write("<Number>\n");
    }
}
