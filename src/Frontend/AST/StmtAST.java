package Frontend.AST;

import Frontend.AST.ExpAST.ExpAST;
import Frontend.AST.ExpAST.LValAST;

public class StmtAST extends BaseAST{
    private ExpAST expAST;
    private LValAST lValAST;
    private int type;

    // return Exp ;
    public StmtAST(ExpAST expAST){
        this.expAST = expAST;
        this.type = 1;
    }

    //  LVal = Exp
    public StmtAST(LValAST lValAST, ExpAST expAST){
        this.lValAST = lValAST;
        this.expAST = expAST;
        this.type = 2;
    }

    public ExpAST getExpAST() {
        return expAST;
    }

    public LValAST getlValAST() {
        return lValAST;
    }

    public int getType() {
        return type;
    }
}
