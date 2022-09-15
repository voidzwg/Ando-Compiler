package Frontend.AST;

import Frontend.AST.ExpAST.ExpAST;
import Frontend.AST.ExpAST.LValAST;

public class StmtAST extends BaseAST{
    private ExpAST expAST;
    private LValAST lValAST;
    private BlockAST blockAST;
    private StmtAST ifStmtAST;
    private StmtAST elseStmtAST;
    private StmtAST loopStmt;
    private CondAST condAST;
    private BreakAST breakAST;
    private ContinueAST continueAST;
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

    //  if( Cond ) Stmt
    public StmtAST(StmtAST ifStmtAST, CondAST condAST) {
        this.condAST = condAST;
        this.ifStmtAST = ifStmtAST;
        this.type = 5;
    }

    //  if( Cond ) Stmt else Stmt
    public StmtAST(StmtAST ifStmtAST, CondAST condAST, StmtAST elseStmtAST) {
        this.condAST = condAST;
        this.ifStmtAST = ifStmtAST;
        this.elseStmtAST = elseStmtAST;
        this.type = 6;
    }

    public StmtAST(CondAST condAST, StmtAST loopStmt){
        this.condAST = condAST;
        this.loopStmt = loopStmt;
        this.type = 7;
    }

    public StmtAST(ContinueAST continueAST) {
        this.continueAST = continueAST;
        this.type = 8;
    }

    public StmtAST(BreakAST breakAST) {
        this.breakAST = breakAST;
        this.type = 9;
    }

    public ExpAST getExpAST() {
        return expAST;
    }

    public LValAST getLValAST() {
        return lValAST;
    }

    public BlockAST getBlockAST() {
        return blockAST;
    }

    public CondAST getCondAST() {
        return condAST;
    }

    public boolean isHasExp() {
        return hasExp;
    }

    public StmtAST getIfStmtAST() {
        return ifStmtAST;
    }

    public StmtAST getElseStmtAST() {
        return elseStmtAST;
    }

    public StmtAST getLoopStmt(){ return loopStmt; }

    public int getType() {
        return type;
    }
}
