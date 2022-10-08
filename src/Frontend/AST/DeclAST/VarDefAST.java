package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ConstExpAST;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class VarDefAST {
    private String ident;
    private InitValAST initValAST;
    private ArrayList<ConstExpAST> constExpASTS;
    private int type;
    private int line;

    public VarDefAST(String ident, int line) {
        this.ident = ident;
        this.type = 1;
        this.line = line;
    }

    public VarDefAST(String ident, InitValAST initValAST, int line) {
        this.ident = ident;
        this.initValAST = initValAST;
        this.type = 2;
        this.line = line;
    }

    public VarDefAST(String ident, ArrayList<ConstExpAST> constExpASTS, int line) {
        this.ident = ident;
        this.constExpASTS = constExpASTS;
        this.type = 3;
        this.line = line;
    }

    public VarDefAST(String ident, InitValAST initValAST, ArrayList<ConstExpAST> constExpASTS, int line) {
        this.ident = ident;
        this.initValAST = initValAST;
        this.constExpASTS = constExpASTS;
        this.type = 4;
        this.line = line;
    }

    public String getIdent() {
        return ident;
    }

    public InitValAST getInitValAST() {
        return initValAST;
    }

    public ArrayList<ConstExpAST> getConstExpASTS() {
        return constExpASTS;
    }

    public int getType() {
        return type;
    }

    public int getLine() {
        return line;
    }
}
