package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ConstExpAST;

import java.util.ArrayList;

public class ConstDefAST {
    String ident;
    ArrayList<ConstExpAST> constExpASTS;
    ConstInitValAST constInitValAST;
    private int len;
    private int type;

    public ConstDefAST(String ident, ConstInitValAST constInitValAST, int len) {
        this.ident = ident;
        this.constInitValAST = constInitValAST;
        this.type = 1;
        this.len = len;
    }

    public ConstDefAST(String ident, ArrayList<ConstExpAST> constExpASTS ,ConstInitValAST constInitValAST, int len) {
        this.ident = ident;
        this.constInitValAST = constInitValAST;
        this.constExpASTS = constExpASTS;
        this.type = 2;
        this.len = len;
    }

    public String getIdent() {
        return ident;
    }

    public ConstInitValAST getConstInitValAST() {
        return constInitValAST;
    }

    public ArrayList<ConstExpAST> getConstExpASTS() {
        return constExpASTS;
    }

    public int getType() {
        return type;
    }

    public int getLen() {
        return len;
    }
}
