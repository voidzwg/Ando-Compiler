package Frontend.AST;

import Frontend.AST.ExpAST.ConstExpAST;
import Frontend.AST.ExpAST.ExpAST;

import java.util.ArrayList;

public class FuncFParamAST {
    String bType;
    String ident;
    ArrayList<ConstExpAST> constExpASTS;
    private int type;

    public FuncFParamAST(String bType, String ident) {
        this.bType = bType;
        this.ident = ident;
        this.type = 1;
    }

    public FuncFParamAST(String bType, String ident, ArrayList<ConstExpAST> constExpASTS) {
        this.bType = bType;
        this.ident = ident;
        this.constExpASTS = constExpASTS;
        this.type = 2;
    }

    public String getbType() {
        return bType;
    }

    public String getIdent() {
        return ident;
    }

    public ArrayList<ConstExpAST> getConstExpASTS() {
        return constExpASTS;
    }

    public int getType() {
        return type;
    }
}
