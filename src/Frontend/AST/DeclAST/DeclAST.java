package Frontend.AST.DeclAST;

public class DeclAST {
    private ConstDeclAST constDeclAST;
    private VarDeclAST varDeclAST;
    private int type;

    public DeclAST(ConstDeclAST constDeclAST){
        this.constDeclAST = constDeclAST;
        this.type = 1;
    }

    public DeclAST(VarDeclAST varDeclAST) {
        this.varDeclAST = varDeclAST;
        this.type = 2;
    }

    public ConstDeclAST getConstDeclAST() {
        return constDeclAST;
    }

    public VarDeclAST getVarDeclAST() {
        return varDeclAST;
    }

    public int getType() {
        return type;
    }
}
