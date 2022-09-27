package IR.Type;

public class StringType extends Type{
    private String val;
    public StringType(String val){
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
