package AST;

public class BlockAST {
    StmtAST stmtAST;

    public BlockAST(StmtAST stmtAST) {
        this.stmtAST = stmtAST;
    }

    public StmtAST getStmtAST() {
        return stmtAST;
    }
}
