package Frontend.AST;

public class NumberAST extends BaseAST{
    private int intConst;

    public NumberAST(int intConst){
        this.intConst = intConst;
    }

    public int getIntConst() {
        return intConst;
    }
}
