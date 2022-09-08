package IR.Value;

import IR.Type.Type;

public class Argument extends Value{
    private Function parentFunc;

    public Argument(String name, Type type){
        super(name, type);
    }

    public Argument(String name, Type type, Function parentFunc){
        super(name, type);
        this.parentFunc = parentFunc;
    }
}
