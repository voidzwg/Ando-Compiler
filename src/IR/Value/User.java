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

    public void addOperand(Use use){
        operandList.add(use);
    }

}
