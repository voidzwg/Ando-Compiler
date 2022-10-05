package IR.Type;

public class StringType extends Type{
    private String val;
    //  mode为0代表是printf，为1则为scanf
    private int mode;
    public StringType(String val, int mode){
        this.val = val;
        this.mode = mode;
    }

    public String getVal() {
        return val;
    }

    public int getMode() {
        return mode;
    }
}
