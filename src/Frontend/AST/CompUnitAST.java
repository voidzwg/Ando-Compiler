package Frontend.AST;

import Frontend.AST.DeclAST.DeclAST;

import java.util.ArrayList;

public class CompUnitAST {
    ArrayList<FuncDefAST> funcDefASTS;
    ArrayList<DeclAST> declASTS;

    public CompUnitAST(ArrayList<DeclAST> declASTS ,ArrayList<FuncDefAST> funcDefASTS) {
        this.funcDefASTS = funcDefASTS;
        this.declASTS = declASTS;
    }

    public ArrayList<FuncDefAST> getFuncDefASTS() {
        return funcDefASTS;
    }

    public ArrayList<DeclAST> getDeclASTS() {
        return declASTS;
    }
}
