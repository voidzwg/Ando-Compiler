package Frontend.AST.DeclAST;

import java.util.ArrayList;

public class VarDeclAST {
    private ArrayList<VarDefAST> varDefASTS;

    public VarDeclAST(){
        this.varDefASTS = new ArrayList<>();
    }

    public ArrayList<VarDefAST> getVarDefASTS() {
        return varDefASTS;
    }

    public void addVarDef(VarDefAST varDefAST){
        this.varDefASTS.add(varDefAST);
    }

}
