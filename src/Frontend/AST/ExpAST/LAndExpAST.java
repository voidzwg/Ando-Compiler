package Frontend.AST.ExpAST;

public class LAndExpAST {
    private EqExpAST eqExpAST;
    private LAndExpAST lAndExpAST;
    private int type;

    public LAndExpAST(EqExpAST eqExpAST) {
        this.eqExpAST = eqExpAST;
        this.type = 1;
    }

    public LAndExpAST(EqExpAST eqExpAST, LAndExpAST lAndExpAST) {
        this.eqExpAST = eqExpAST;
        this.lAndExpAST = lAndExpAST;
        this.type = 2;
    }

    public EqExpAST getEqExpAST() {
        return eqExpAST;
    }

    public LAndExpAST getLAndExpAST() {
        return lAndExpAST;
    }

    public int getType() {
        return type;
    }
}
