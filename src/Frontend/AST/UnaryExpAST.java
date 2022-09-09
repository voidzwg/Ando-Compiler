package Frontend.AST;

public class UnaryExpAST {
    private PrimaryExpAST primaryExpAST;
    private String unaryOP;
    private UnaryExpAST unaryExpAST;
    //  type表示改UnaryExp的类型，按顺序依次为1，2
    private int type;

    public UnaryExpAST(PrimaryExpAST primaryExpAST) {
        this.primaryExpAST = primaryExpAST;
        this.type = 1;
    }

    public UnaryExpAST(String unaryOP, UnaryExpAST unaryExpAST) {
        this.unaryOP = unaryOP;
        this.unaryExpAST = unaryExpAST;
        this.type = 2;
    }

    public PrimaryExpAST getPrimaryExpAST() {
        return primaryExpAST;
    }

    public String getUnaryOP() {
        return unaryOP;
    }

    public UnaryExpAST getUnaryExpAST() {
        return unaryExpAST;
    }

    public int getType() {
        return type;
    }
}
