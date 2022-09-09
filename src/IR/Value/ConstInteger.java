package IR.Value;

import IR.Type.IntegerType;

public class ConstInteger extends Constant{
    private int val;

    public ConstInteger(int val){
        super("", new IntegerType(32));
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
