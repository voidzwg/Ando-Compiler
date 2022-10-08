package Frontend.AST;

import java.util.ArrayList;

public class FuncDefAST extends BaseAST{
    String funcType;
    String ident;
    BlockAST blockAST;
    FuncFParamsAST funcFParamsAST;
    private int type;
    private int line;

    //  Constructor
    public FuncDefAST(String funcType, String ident, BlockAST blockAST, int line) {
        this.funcType = funcType;
        this.ident = ident;
        this.blockAST = blockAST;
        this.type = 1;
        this.line = line;
    }

    public FuncDefAST(String funcType, String ident, BlockAST blockAST, FuncFParamsAST funcFParamsAST, int line) {
        this.funcType = funcType;
        this.ident = ident;
        this.blockAST = blockAST;
        this.funcFParamsAST = funcFParamsAST;
        this.type = 2;
        this.line = line;
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

    public int getLine() {
        return line;
    }
}
