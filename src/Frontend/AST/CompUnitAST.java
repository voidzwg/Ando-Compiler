package Frontend.AST;

public class CompUnitAST {
    FuncDefAST funcDefAST;

    public CompUnitAST(FuncDefAST funcDefAST) {
        this.funcDefAST = funcDefAST;
    }

    public FuncDefAST getFuncDefAST() {
        return funcDefAST;
    }
}
