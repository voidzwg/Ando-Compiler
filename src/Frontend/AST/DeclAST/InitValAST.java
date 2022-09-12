package Frontend.AST.DeclAST;

import Frontend.AST.ExpAST.ExpAST;

public class InitValAST {
    private ExpAST expAST;

    public InitValAST(ExpAST expAST) {
        this.expAST = expAST;
    }

    public ExpAST getExpAST() {
        return expAST;
    }
}
