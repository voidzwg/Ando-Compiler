package IR.Value.Instructions;

import IR.Type.IntegerType;
import IR.Type.Type;
import IR.Value.*;

import java.util.ArrayList;

public class CallInst extends Instruction {
    private ArrayList<Value> values = new ArrayList<>();
    public CallInst(BasicBlock basicBlock, Function function, ArrayList<Value> values) {
        super("", function.getType(), OP.Call, basicBlock);
        this.values = values;
        if(function.getType().isIntegerTy()){
            this.setName("%" + (++Value.valNumber));
        }
        this.addOperand(function);
        for (Value value : values) {
            this.addOperand(value);
        }
    }

    public CallInst(BasicBlock basicBlock, Function function) {
        super("", function.getType(), OP.Call, basicBlock);
        if(function.getType().isIntegerTy()){
            this.setName("%" + (++Value.valNumber));
        }
        this.addOperand(function);
    }

    public Value getCallFunc(){
        return getOperandList().get(0).getValue();
    }

    public ArrayList<Value> getValues(){
        return values;
    }
}
