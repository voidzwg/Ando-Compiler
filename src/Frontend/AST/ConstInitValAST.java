package Frontend.AST;

import Frontend.AST.ExpAST.AddExpAST;

public class ConstInitValAST {
    AddExpAST addExpAST;

    public ConstInitValAST(AddExpAST addExpAST){
        this.addExpAST = addExpAST;
    }
}
