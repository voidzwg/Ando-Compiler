package Frontend.AST;

import Frontend.AST.DeclAST.DeclAST;

public class BlockItemAST {
    private DeclAST declAST;
    private StmtAST stmtAST;
    private int type;

    public BlockItemAST(DeclAST declAST) {
        this.declAST = declAST;
        this.type = 1;
    }

    public BlockItemAST(StmtAST stmtAST) {
        this.stmtAST = stmtAST;
        this.type = 2;
    }

    public DeclAST getDeclAST() {
        return declAST;
    }

    public StmtAST getStmtAST() {
        return stmtAST;
    }

    public int getType() {
        return type;
    }
}
