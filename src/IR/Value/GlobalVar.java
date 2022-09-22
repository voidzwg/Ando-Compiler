package IR.Value;

import IR.Type.IntegerType;
import IR.Type.Type;

public class GlobalVar extends Value{
    private boolean isConst;
    private Value value;

    public GlobalVar(String name, boolean isConst, Value value){
        super(name, new IntegerType(32));
        this.isConst = isConst;
        this.value = value;
    }

    public boolean isConst() {
        return isConst;
    }

    public Value getValue() {
        return value;
    }
}
