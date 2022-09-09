package IR.Value;

import IR.Type.Type;

public abstract class Constant extends Value{
    public Constant(String name, Type type){
        super(name, type);
    }
}
