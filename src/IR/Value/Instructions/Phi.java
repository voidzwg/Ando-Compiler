package IR.Value.Instructions;

import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

import java.util.ArrayList;

public class Phi extends Instruction {

    public Phi(Type type, BasicBlock basicBlock, ArrayList<Value> values) {
        super("%" + (++Value.valNumber), type, OP.Phi, basicBlock, true);
        for(Value value : values){
            addOperand(value);
        }
    }

    @Override
    public String toString() {
        ArrayList<Value> useValues = getUseValues();
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append(" = phi ");
        sb.append(getType()).append(" ");
        for (int i = 0; i < useValues.size(); i++) {
            Value useValue = useValues.get(i);
            if (i != 0) {
                sb.append(",");
            }
            sb.append("[ ");
            sb.append(useValue.getName()).append(", %");
        }
        return sb.toString();
    }
}
