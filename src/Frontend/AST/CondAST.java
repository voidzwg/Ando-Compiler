package Frontend.AST;

import Frontend.AST.ExpAST.LOrExpAST;

public class CondAST {
    private LOrExpAST lOrExpAST;

    public CondAST(LOrExpAST lOrExpAST) {
        this.lOrExpAST = lOrExpAST;
    }

    public LOrExpAST getLOrExpAST() {
        return lOrExpAST;
    }
}
