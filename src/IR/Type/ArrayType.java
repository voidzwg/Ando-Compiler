package IR.Type;

import java.awt.*;
import java.util.ArrayList;

public class ArrayType extends PointerType{
    int eleDim;

    public ArrayType(Type eleType, int eleDim) {
        super(eleType);
        this.eleDim = eleDim;
    }

    @Override
    public boolean isArrayType(){
        return true;
    }

    public int getEleDim() {
        return eleDim;
    }
}
