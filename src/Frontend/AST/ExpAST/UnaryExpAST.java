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
    private int line;

    public UnaryExpAST(PrimaryExpAST primaryExpAST, int line) {
        this.primaryExpAST = primaryExpAST;
        this.type = 1;
        this.line = line;
    }

    public UnaryExpAST(String unaryOP, UnaryExpAST unaryExpAST, int line) {
        this.unaryOP = unaryOP;
        this.unaryExpAST = unaryExpAST;
        this.type = 2;
        this.line = line;
    }

    public UnaryExpAST(String ident, FuncRParamsAST funcRParamsAST, int line) {
        this.ident = ident;
        this.funcRParamsAST = funcRParamsAST;
        this.type = 3;
        this.line = line;
    }

    public UnaryExpAST(String ident, int line) {
        this.ident = ident;
        this.type = 4;
        this.line = line;
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

    public int getLine() {
        return line;
    }
}
