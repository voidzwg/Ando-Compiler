package IR.Value.Instructions;

import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GepInst extends Instruction {

    Value target;
    //  索引
    ArrayList<Integer> indexs;


    public GepInst(ArrayList<Integer> indexs, Value target, Type type, BasicBlock basicBlock) {
        super("%" + (++Value.valNumber), type, OP.GEP, basicBlock);
        this.indexs = indexs;
        this.target = target;
    }

    public Value getTarget() {
        return target;
    }

    public ArrayList<Integer> getIndexs() {
        return indexs;
    }
}
