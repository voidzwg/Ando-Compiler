package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ExpAST;

import java.util.ArrayList;

public class InitValAST {
    private ExpAST expAST;
    private ArrayList<InitValAST> initValASTS;
    private int type;

    public InitValAST(ExpAST expAST) {
        this.expAST = expAST;
        this.type = 1;
    }

    public InitValAST(ArrayList<InitValAST> initValASTS) {
        this.initValASTS = initValASTS;
        this.type = 2;
    }

    public ExpAST getExpAST() {
        return expAST;
    }

    public ArrayList<InitValAST> getInitValASTS() {
        return initValASTS;
    }

    public int getType() {
        return type;
    }
}
