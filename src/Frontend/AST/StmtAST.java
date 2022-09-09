package Frontend.AST;

public class StmtAST extends BaseAST{
    NumberAST numberAST;

    public StmtAST(NumberAST numberAST){
        this.numberAST = numberAST;
    }

    public NumberAST getNumberAST() {
        return numberAST;
    }
}
