package Frontend.AST;

public class FuncFParamAST {
    String bType;
    String ident;

    public FuncFParamAST(String bType, String ident) {
        this.bType = bType;
        this.ident = ident;
    }

    public String getbType() {
        return bType;
    }

    public String getIdent() {
        return ident;
    }
}
