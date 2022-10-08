package IR.Value.Instructions;

import IR.Type.ArrayType;
import IR.Type.IntegerType;
import IR.Type.PointerType;
import IR.Type.Type;
import IR.Value.BasicBlock;
import IR.Value.Instruction;
import IR.Value.Value;

import java.awt.*;
import java.sql.Array;
import java.util.ArrayList;

public class GepInst extends Instruction {

    Value target;
    //  索引
    ArrayList<Value> indexs;

    public GepInst(ArrayList<Value> indexs, Value target, Type type, BasicBlock basicBlock) {
        super("%" + (++Value.valNumber), type, OP.GEP, basicBlock, true);
        this.indexs = indexs;
        this.target = target;
    }

    public Value getTarget() {
        return target;
    }

    public ArrayList<Value> getIndexs() {
        return indexs;
    }
}
