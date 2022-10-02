package IR.Value;

import IR.Type.VoidType;

import java.util.ArrayList;

public class ConstArray extends Value{
    private ArrayList<Integer> arrayValues;
    private ArrayList<Integer> dimList;
    private String ident;

    public ConstArray(String ident, ArrayList<Integer> dimList, ArrayList<Integer> arrayValues){
        super(ident, new VoidType());
        this.dimList = dimList;
        this.arrayValues = arrayValues;
    }

    public ArrayList<Integer> getArrayValues() {
        return arrayValues;
    }

    public ArrayList<Integer> getDimList() {
        return dimList;
    }

    public String getIdent() {
        return ident;
    }
}
