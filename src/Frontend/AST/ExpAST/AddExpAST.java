package Frontend.AST.ExpAST;

public class AddExpAST {
    private MulExpAST mulExpAST;
    private AddExpAST addExpAST;
    private String op;
    private int type;

    public AddExpAST(MulExpAST mulExpAST){
        this.mulExpAST = mulExpAST;
        this.type = 1;
    }

    public AddExpAST(MulExpAST mulExpAST, String op, AddExpAST addExpAST){
        this.mulExpAST = mulExpAST;
        this.addExpAST = addExpAST;
        this.op = op;
        this.type = 2;
    }

    public MulExpAST getMulExpAST() {
        return mulExpAST;
    }

    public AddExpAST getAddExpAST() {
        return addExpAST;
    }

    public String getOp() {
        return op;
    }

    public int getType(){
        return type;
    }
}
