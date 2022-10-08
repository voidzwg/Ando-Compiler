package Frontend.AST.ExpAST;

import java.util.ArrayList;

public class LValAST {
    String ident;
    ArrayList<ExpAST> expASTS;
    private int type;
    private int line;

    public LValAST(String ident, int line){
        this.ident = ident;
        this.type = 1;
        this.line = line;
    }

    public LValAST(String ident, ArrayList<ExpAST> expASTS, int line){
        this.ident = ident;
        this.expASTS = expASTS;
        this.type = 2;
        this.line = line;
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

    public int getLine() {
        return line;
    }
}
