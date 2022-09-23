package IR.Type;

import java.util.ArrayList;

public class ArrayType extends Type{
    private Type eleType;
    ArrayList<Integer> eleDim;

    public ArrayType(Type eleType, ArrayList<Integer> eleDim) {
        this.eleType = eleType;
        this.eleDim = eleDim;
    }

    public ArrayType(){

    }


    @Override
    public boolean isArrayType(){
        return true;
    }

    public ArrayList<Integer> getEleDim() {
        return eleDim;
    }
}
