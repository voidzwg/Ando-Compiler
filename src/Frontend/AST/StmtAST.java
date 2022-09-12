package Frontend.AST;

import Frontend.AST.ExpAST.ExpAST;
import Frontend.AST.ExpAST.LValAST;

public class StmtAST extends BaseAST{
    private ExpAST expAST;
    private LValAST lValAST;
    private BlockAST blockAST;
    //  对于[Exp]表示是否有Exp
    private boolean hasExp;
    private final int type;

    // return Exp ;
    public StmtAST(ExpAST expAST){
        this.expAST = expAST;
        this.type = 1;
    }

    //  LVal = Exp
    public StmtAST(LValAST lValAST, ExpAST expAST){
        this.lValAST = lValAST;
        this.expAST = expAST;
        this.type = 2;
    }

    //  Block
    public StmtAST(BlockAST blockAST){
        this.blockAST = blockAST;
        this.type = 3;
    }

    //  [Exp] ';'
    public StmtAST(ExpAST expAST, boolean hasExp){
        this.expAST = expAST;
        this.hasExp = hasExp;
        this.type = 4;
    }

    public ExpAST getExpAST() {
        return expAST;
    }

    public LValAST getlValAST() {
        return lValAST;
    }

    public BlockAST getBlockAST() {
        return blockAST;
    }

    public int getType() {
        return type;
    }
}
