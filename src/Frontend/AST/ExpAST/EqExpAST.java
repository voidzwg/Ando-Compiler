package Frontend.AST.ExpAST;

public class EqExpAST {
    private RelExpAST relExpAST;
    private EqExpAST eqExpAST;
    private String op;
    private int type;

    public EqExpAST(RelExpAST relExpAST) {
        this.relExpAST = relExpAST;
        this.type = 1;
    }

    public EqExpAST(RelExpAST relExpAST, String op, EqExpAST eqExpAST) {
        this.relExpAST = relExpAST;
        this.eqExpAST = eqExpAST;
        this.op = op;
        this.type = 2;
    }

    public RelExpAST getRelExpAST() {
        return relExpAST;
    }

    public EqExpAST getEqExpAST() {
        return eqExpAST;
    }

    public String getOp() {
        return op;
    }

    public int getType() {
        return type;
    }
}
