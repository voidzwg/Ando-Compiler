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

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        if(!EleType.isArrayType()){
            stringBuilder.append("[");
            stringBuilder.append(eleDim);
            stringBuilder.append(" x i32]");
        }
        else{
            stringBuilder.append("[");
            stringBuilder.append(eleDim);
            stringBuilder.append(" x ");
            stringBuilder.append(EleType.toString());
            stringBuilder.append("]");
        }
        //  这里的StringBuilder中有eleType产生的*号，需要我们处理
        String rawString = stringBuilder.toString();
        String string =  rawString.replace("*", "");
        return string + "*";
    }
}
