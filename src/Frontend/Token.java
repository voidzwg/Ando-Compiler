package Frontend;

public class Token {
    private Tokens type;
    private String val;
    private int line;

    public Token(Tokens type, String val, int line){
        this.type = type;
        this.val = val;
        this.line = line;
    }

    public Tokens getType() {
        return type;
    }

    public String getVal() {
        return val;
    }

    public int getLine() {
        return line;
    }
}
