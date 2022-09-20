package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ConstExpAST;

import java.util.ArrayList;

public class ConstDefAST {
    String ident;
    ArrayList<ConstExpAST> constExpASTS;
    ConstInitValAST constInitValAST;
    private int type;

    public ConstDefAST(String ident, ConstInitValAST constInitValAST) {
        this.ident = ident;
        this.constInitValAST = constInitValAST;
        this.type = 1;
    }

    public ConstDefAST(String ident, ArrayList<ConstExpAST> constExpASTS ,ConstInitValAST constInitValAST) {
        this.ident = ident;
        this.constInitValAST = constInitValAST;
        this.constExpASTS = constExpASTS;
        this.type = 2;
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
}
