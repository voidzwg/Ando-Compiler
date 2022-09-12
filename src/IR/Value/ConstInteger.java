package IR.Value;

import IR.Type.IntegerType;

public class ConstInteger extends Constant{
    private final int val;

    public static ConstInteger constZero = new ConstInteger(0);

    public ConstInteger(int val){
        super(String.valueOf(val), new IntegerType(32));
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
