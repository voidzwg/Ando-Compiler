package Frontend.AST.ExpAST;

public class MulExpAST {
    private UnaryExpAST unaryExpAST;
    private MulExpAST mulExpAST;
    private String op;
    private int type;

    public MulExpAST(UnaryExpAST unaryExpAST){
        this.unaryExpAST = unaryExpAST;
        this.type = 1;
    }

    public MulExpAST(UnaryExpAST unaryExpAST, String op, MulExpAST mulExpAST){
        this.unaryExpAST = unaryExpAST;
        this.mulExpAST = mulExpAST;
        this.op = op;
        this.type = 2;
    }

    public UnaryExpAST getUnaryExpAST() {
        return unaryExpAST;
    }

    public MulExpAST getMulExpAST() {
        return mulExpAST;
    }

    public String getOp() {
        return op;
    }

    public int getType() {
        return type;
    }
}
