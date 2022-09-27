package IR.Value;

import IR.Type.IntegerType;
import IR.Type.Type;

import java.util.ArrayList;

public class GlobalVar extends Value{
    private boolean isConst;
    private Value value;
    //  代表全局数组的初始值
    private ArrayList<Value> values;

    public GlobalVar(String name, Type type, boolean isConst, Value value){
        super(name, type);
        this.isConst = isConst;
        //  这个Value是他的初始值
        this.value = value;
    }

    public GlobalVar(String name, Type type, boolean isConst, ArrayList<Value> values){
        super(name, type);
        this.isConst = isConst;
        //  这个Value是他的初始值
        this.values = values;
    }

    public boolean isConst() {
        return isConst;
    }

    public Value getValue() {
        return value;
    }

    public ArrayList<Value> getValues() {
        return values;
    }
}
