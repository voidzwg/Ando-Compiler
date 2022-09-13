package Frontend.AST.ExpAST;

public class RelExpAST {
    private AddExpAST addExpAST;
    private RelExpAST relExpAST;
    private String op;
    private int type;

    public RelExpAST(AddExpAST addExpAST){
        this.addExpAST = addExpAST;
        this.type = 1;
    }

    public RelExpAST(AddExpAST addExpAST,String op, RelExpAST relExpAST) {
        this.addExpAST = addExpAST;
        this.relExpAST = relExpAST;
        this.op = op;
        this.type = 2;
    }

    public AddExpAST getAddExpAST() {
        return addExpAST;
    }

    public RelExpAST getRelExpAST() {
        return relExpAST;
    }

    public String getOp() {
        return op;
    }

    public int getType() {
        return type;
    }
}
