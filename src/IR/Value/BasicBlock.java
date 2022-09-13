package IR.Value;

import IR.Type.LabelType;

import java.util.ArrayList;

public class BasicBlock extends Value{
    private Function parentFunc;
    private ArrayList<Instruction> insts;

    public static int blockNum = 0;

    public BasicBlock(){
        super("block" + String.valueOf(++blockNum), new LabelType());
        this.insts = new ArrayList<>();
    }

    public BasicBlock(Function function){
        super("block" + String.valueOf(++blockNum), new LabelType());
        this.insts = new ArrayList<>();
        this.parentFunc = function;
    }

    //  Main Methods
    public void addInst(Instruction inst){
        this.insts.add(inst);
    }


    //  Getters and Setters
    public void setParentFunc(Function parentFunc) {
        this.parentFunc = parentFunc;
    }

    public ArrayList<Instruction> getInsts() {
        return insts;
    }
}
