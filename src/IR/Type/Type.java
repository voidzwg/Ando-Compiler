package IR.Type;

public abstract class Type {
    public Type(){}

    public boolean isIntegerTy(){
        return false;
    }

    public boolean isFunctionTy() { return false; }

    public boolean isLabelTy() { return false; }

    public boolean isVoidTy() { return false; }

    public boolean isArrayType() {
        return false;
    }

}
