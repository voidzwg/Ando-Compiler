package Frontend.AST;

public class DeclAST {
    private ConstDeclAST constDeclAST;

    public DeclAST(ConstDeclAST constDeclAST){
        this.constDeclAST = constDeclAST;
    }

    public ConstDeclAST getConstDeclAST() {
        return constDeclAST;
    }
}
