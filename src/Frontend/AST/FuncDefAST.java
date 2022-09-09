package Frontend.AST;

public class FuncDefAST extends BaseAST{
    String funcType;
    String ident;
    BlockAST blockAST;

    //  Constructor
    public FuncDefAST(String funcType, String ident, BlockAST blockAST) {
        this.funcType = funcType;
        this.ident = ident;
        this.blockAST = blockAST;
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
}
