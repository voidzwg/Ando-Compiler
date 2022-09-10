package Frontend.AST.ExpAST;

public class ExpAST {
    AddExpAST addExpAST;

    public ExpAST(AddExpAST addExpAST) {
        this.addExpAST = addExpAST;
    }

    public AddExpAST getAddExpAST() {
        return addExpAST;
    }

}
