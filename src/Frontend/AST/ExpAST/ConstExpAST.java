package Frontend.AST.ExpAST;

public class ConstExpAST {
    private AddExpAST addExpAST;

    public ConstExpAST(AddExpAST addExpAST) {
        this.addExpAST = addExpAST;
    }

    public AddExpAST getAddExpAST(){
        return addExpAST;
    }
}
