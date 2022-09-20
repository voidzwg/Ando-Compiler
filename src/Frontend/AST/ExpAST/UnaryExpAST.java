package Frontend.AST.ExpAST;

import Frontend.AST.FuncRParamsAST;

public class UnaryExpAST {
    private PrimaryExpAST primaryExpAST;
    private String unaryOP;
    private UnaryExpAST unaryExpAST;
    private String ident;
    private FuncRParamsAST funcRParamsAST;
    //  type表示改UnaryExp的类型，按顺序依次为1，2
    private int type;

    public UnaryExpAST(PrimaryExpAST primaryExpAST) {
        this.primaryExpAST = primaryExpAST;
        this.type = 1;
    }

    public UnaryExpAST(String unaryOP, UnaryExpAST unaryExpAST) {
        this.unaryOP = unaryOP;
        this.unaryExpAST = unaryExpAST;
        this.type = 2;
    }

    public UnaryExpAST(String ident, FuncRParamsAST funcRParamsAST) {
        this.ident = ident;
        this.funcRParamsAST = funcRParamsAST;
        this.type = 3;
    }

    public UnaryExpAST(String ident) {
        this.ident = ident;
        this.type = 4;
    }

    public PrimaryExpAST getPrimaryExpAST() {
        return primaryExpAST;
    }

    public String getUnaryOP() {
        return unaryOP;
    }

    public UnaryExpAST getUnaryExpAST() {
        return unaryExpAST;
    }

    public String getIdent() {
        return ident;
    }

    public FuncRParamsAST getFuncRParamsAST() {
        return funcRParamsAST;
    }

    public int getType() {
        return type;
    }
}
