package Frontend.AST.DeclAST;

public class ConstDefAST {
    String ident;
    ConstInitValAST constInitValAST;

    public ConstDefAST(String ident, ConstInitValAST constInitValAST) {
        this.ident = ident;
        this.constInitValAST = constInitValAST;
    }

    public String getIdent() {
        return ident;
    }

    public ConstInitValAST getConstInitValAST() {
        return constInitValAST;
    }
}
