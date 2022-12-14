package IR.Value.Instructions;

import IR.Type.IntegerType;
import IR.Type.StringType;
import IR.Type.Type;
import IR.Value.*;

import java.util.ArrayList;

public class CallInst extends Instruction {
    private ArrayList<Value> values = new ArrayList<>();
    public CallInst(BasicBlock basicBlock, Function function, ArrayList<Value> values) {
        super("", function.getType(), OP.Call, basicBlock, false);
        this.values = values;
        if(function.getType().isIntegerTy()){
            this.setName("%" + (++Value.valNumber));
            this.setHasName(true);
        }
        this.addOperand(function);
        for (Value value : values) {
            this.addOperand(value);
        }
    }

    public CallInst(BasicBlock basicBlock, Function function) {
        super("", function.getType(), OP.Call, basicBlock, false);
        if(function.getType().isIntegerTy()){
            this.setName("%" + (++Value.valNumber));
            this.setHasName(true);
        }
        this.addOperand(function);
    }

    public Value getCallFunc(){
        return getOperandList().get(0).getValue();
    }

    public String getFString() {
        StringType strType = (StringType) getOperandList().get(1).getValue().getType();
        return strType.getVal();
    }

    public ArrayList<Value> getValues(){
        return values;
    }
}
