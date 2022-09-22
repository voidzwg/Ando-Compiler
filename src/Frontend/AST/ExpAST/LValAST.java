package Frontend.AST.ExpAST;

import java.util.ArrayList;

public class LValAST {
    String ident;
    ArrayList<ExpAST> expASTS;
    private int type;

    public LValAST(String ident){
        this.ident = ident;
        this.type = 1;
    }

    public LValAST(String ident, ArrayList<ExpAST> expASTS){
        this.ident = ident;
        this.expASTS = expASTS;
        this.type = 2;
    }

    public String getIdent() {
        return ident;
    }

    public ArrayList<ExpAST> getExpASTS() {
        return expASTS;
    }

    public int getType() {
        return type;
    }
}
