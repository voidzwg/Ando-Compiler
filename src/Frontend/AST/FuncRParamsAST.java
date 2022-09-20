package Frontend.AST;

import Frontend.AST.ExpAST.ExpAST;

import java.util.ArrayList;

public class FuncRParamsAST {
    private ArrayList<ExpAST> expASTS;

    public FuncRParamsAST(ArrayList<ExpAST> expASTS) {
        this.expASTS = expASTS;
    }

    public ArrayList<ExpAST> getExpASTS() {
        return expASTS;
    }
}
