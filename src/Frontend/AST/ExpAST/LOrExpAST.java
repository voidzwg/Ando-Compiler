package Frontend.AST.ExpAST;

public class LOrExpAST {
    private LAndExpAST lAndExpAST;
    private LOrExpAST lOrExpAST;
    private int type;

    public LOrExpAST(LAndExpAST lAndExpAST) {
        this.lAndExpAST = lAndExpAST;
        this.type = 1;
    }

    public LOrExpAST(LAndExpAST lAndExpAST, LOrExpAST lOrExpAST) {
        this.lAndExpAST = lAndExpAST;
        this.lOrExpAST = lOrExpAST;
        this.type = 2;
    }

    public LAndExpAST getLAndExpAST() {
        return lAndExpAST;
    }

    public LOrExpAST getLOrExpAST() {
        return lOrExpAST;
    }

    public int getType() {
        return type;
    }
}
