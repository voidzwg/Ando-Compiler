package Frontend.AST;

import java.util.ArrayList;

public class ConstDeclAST {
    ArrayList<ConstDefAST> constDefASTS;

    public ConstDeclAST(){
        this.constDefASTS = new ArrayList<>();
    }

    public void addConstDef(ConstDefAST constDefAST){
        this.constDefASTS.add(constDefAST);
    }
}
