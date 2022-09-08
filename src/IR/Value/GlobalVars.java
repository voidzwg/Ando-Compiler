package IR.Value;

import IR.Type.Type;

public class GlobalVars extends Value{
    private boolean isConst;

    public GlobalVars(String name, Type type, boolean isConst){
        super(name, type);
        this.isConst = isConst;
    }
}
