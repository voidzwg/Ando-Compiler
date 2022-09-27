package IR.Type;

public class IntegerType extends Type{
    private int bit;

    public IntegerType(int bit){
        this.bit = bit;
    }

    @Override
    public boolean isIntegerTy(){
        return true;
    }

    @Override
    public String toString(){
        return "i" + bit;
    }
}
