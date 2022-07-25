public class Token {
    private Tokens type;
    private String val;

    public Token(Tokens type, String val){
        this.type = type;
        this.val = val;
    }

    public Tokens getType() {
        return type;
    }

    public String getVal() {
        return val;
    }
}
