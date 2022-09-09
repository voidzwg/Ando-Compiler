package Frontend.AST;

public class ExpAST {
    UnaryExpAST unaryExp;

    public ExpAST(UnaryExpAST unaryExp) {
        this.unaryExp = unaryExp;
    }

    public UnaryExpAST getUnaryExp() {
        return unaryExp;
    }
}
