package IR.Value;

import IR.Type.Type;
import IR.Use;

import java.util.ArrayList;

public class Value {
    private String name;
    private Type type;
    private ArrayList<Use> useList;
    //  valNumber用于给value命名
    public static int valNumber = -1;

    public Value(){}

    public Value(String name, Type type){
        this.name = name;
        this.type = type;
        this.useList = new ArrayList<>();
    }


    @Override
    public String toString(){
//        if(this instanceof ConstInteger) return this.name;
//        else{
//            return this.type + " " + this.name;
//        }
        return this.type + " " + this.name;
    }

    //  Getters and Setters
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public ArrayList<Use> getUseList() {
        return useList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setUseList(ArrayList<Use> useList) {
        this.useList = useList;
    }
}
