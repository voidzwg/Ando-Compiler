package Frontend.AST;

import Frontend.AST.ExpAST.ExpAST;

public class StmtAST extends BaseAST{
    ExpAST expAST;

    public StmtAST(ExpAST expAST){
        this.expAST = expAST;
    }

    public ExpAST getExpAST() {
        return expAST;
    }
}
