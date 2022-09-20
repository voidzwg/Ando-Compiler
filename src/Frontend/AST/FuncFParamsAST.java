package Frontend.AST;

import java.util.ArrayList;

public class FuncFParamsAST {
    private ArrayList<FuncFParamAST> funcFParamASTS;

    public FuncFParamsAST(ArrayList<FuncFParamAST> funcFParamASTS) {
        this.funcFParamASTS = funcFParamASTS;
    }

    public ArrayList<FuncFParamAST> getFuncFParamASTS() {
        return funcFParamASTS;
    }
}
