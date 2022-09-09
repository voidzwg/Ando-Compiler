package IR.Value;

import IR.Type.Type;
import IR.Use;

import java.util.ArrayList;

public class User extends Value{
    private ArrayList<Use> operandList;

    public User(String name, Type type){
        super(name, type);
        this.operandList = new ArrayList<>();
    }

    public void addOperand(Value value){
        Use use = new Use(value, this);
        operandList.add(use);
    }

    public ArrayList<Use> getOperandList() {
        return operandList;
    }
}
