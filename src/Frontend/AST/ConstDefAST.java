package Frontend.AST;

import IR.Value.Value;

public class ConstDefAST {
    String ident;
    ConstInitValAST constInitValAST;

    public ConstDefAST(String ident, ConstInitValAST constInitValAST) {
        this.ident = ident;
        this.constInitValAST = constInitValAST;
    }
}
