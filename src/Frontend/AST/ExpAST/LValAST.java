package Frontend.AST.ExpAST;

public class LValAST {
    String ident;

    public LValAST(String ident){
        this.ident = ident;
    }

    public String getIdent() {
        return ident;
    }
}
