package Frontend.AST.DeclAST;

public class VarDefAST {
    private String ident;
    private InitValAST initValAST;
    private int type;

    public VarDefAST(String ident) {
        this.ident = ident;
        this.type = 1;
    }

    public VarDefAST(String ident, InitValAST initValAST) {
        this.ident = ident;
        this.initValAST = initValAST;
        this.type = 2;
    }

    public String getIdent() {
        return ident;
    }

    public InitValAST getInitValAST() {
        return initValAST;
    }

    public int getType() {
        return type;
    }
}
