package Frontend.AST;

import java.util.ArrayList;

public class CompUnitAST {
    ArrayList<FuncDefAST> funcDefASTS;

    public CompUnitAST(ArrayList<FuncDefAST> funcDefASTS) {
        this.funcDefASTS = funcDefASTS;
    }

    public ArrayList<FuncDefAST> getFuncDefASTS() {
        return funcDefASTS;
    }

}
