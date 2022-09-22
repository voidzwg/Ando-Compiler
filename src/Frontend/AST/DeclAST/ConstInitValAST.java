package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.AddExpAST;
import Frontend.AST.ExpAST.ConstExpAST;

import java.util.ArrayList;

public class ConstInitValAST {
    ConstExpAST constExpAST;
    ArrayList<ConstInitValAST> constInitValASTS;
    private int type;

    public ConstInitValAST(ConstExpAST constExpAST){
        this.constExpAST = constExpAST;
        this.type = 1;
    }

    public ConstInitValAST(ArrayList<ConstInitValAST> constInitValASTS) {
        this.constInitValASTS = constInitValASTS;
        this.type = 2;
    }

    public ConstExpAST getConstExpAST() {
        return constExpAST;
    }

    public ArrayList<ConstInitValAST> getConstInitValASTS() {
        return constInitValASTS;
    }

    public int getType() {
        return type;
    }
}
