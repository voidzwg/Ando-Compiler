package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ConstExpAST;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class VarDefAST {
    private String ident;
    private InitValAST initValAST;
    private ArrayList<ConstExpAST> constExpASTS;
    private int type;

    public VarDefAST(String ident) {
        this.ident = ident;
        this.type = 1;
    }

    public VarDefAST(String ident, InitValAST initValAST) {
        this.ident = ident;
        this.initValAST = initValAST;
        this.type = 2;
    }

    public VarDefAST(String ident, ArrayList<ConstExpAST> constExpASTS) {
        this.ident = ident;
        this.constExpASTS = constExpASTS;
        this.type = 3;
    }

    public VarDefAST(String ident, InitValAST initValAST, ArrayList<ConstExpAST> constExpASTS) {
        this.ident = ident;
        this.initValAST = initValAST;
        this.constExpASTS = constExpASTS;
        this.type = 4;
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
}
