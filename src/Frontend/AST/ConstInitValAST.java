package Frontend.AST;

import Frontend.AST.ExpAST.AddExpAST;
import Frontend.AST.ExpAST.ConstExpAST;

public class ConstInitValAST {
    ConstExpAST constExpAST;

    public ConstInitValAST(ConstExpAST constExpAST){
        this.constExpAST = constExpAST;
    }

    public ConstExpAST getConstExpAST() {
        return constExpAST;
    }
}
