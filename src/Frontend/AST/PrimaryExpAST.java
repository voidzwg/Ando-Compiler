package Frontend.AST;

public class PrimaryExpAST {
    private ExpAST expAST;
    private NumberAST numberAST;
    //  type表示该PrimaryExpAST的类型，1是expAST，2是numberAST
    private final int type;

    public PrimaryExpAST(ExpAST expAST) {
        this.expAST = expAST;
        this.type = 1;
    }

    public PrimaryExpAST(NumberAST numberAST) {
        this.numberAST = numberAST;
        this.type = 2;
    }

    public ExpAST getExpAST() {
        return expAST;
    }

    public NumberAST getNumberAST() {
        return numberAST;
    }

    public int getType() {
        return type;
    }
}
