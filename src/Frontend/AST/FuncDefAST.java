package Frontend.AST;

import java.util.ArrayList;

public class FuncDefAST extends BaseAST{
    String funcType;
    String ident;
    BlockAST blockAST;
    FuncFParamsAST funcFParamsAST;
    private int type;

    //  Constructor
    public FuncDefAST(String funcType, String ident, BlockAST blockAST) {
        this.funcType = funcType;
        this.ident = ident;
        this.blockAST = blockAST;
        this.type = 1;
    }

    public FuncDefAST(String funcType, String ident, BlockAST blockAST, FuncFParamsAST funcFParamsAST) {
        this.funcType = funcType;
        this.ident = ident;
        this.blockAST = blockAST;
        this.funcFParamsAST = funcFParamsAST;
        this.type = 2;
    }

    //  Main Methods


    //  Getters
    public String getFuncType() {
        return funcType;
    }

    public String getIdent() {
        return ident;
    }

    public BlockAST getBlockAST() {
        return blockAST;
    }

    public FuncFParamsAST getFuncFParamsAST() { return funcFParamsAST; }

    public int getType() {
        return type;
    }
}
