package IR.Value;

import IR.Type.LabelType;

import java.util.ArrayList;

public class BasicBlock extends Value{
    private Function parentFunc;
    private ArrayList<Instruction> insts;

    public static int blockNum = 0;

    public BasicBlock(){
        super("%" + String.valueOf(++blockNum), new LabelType());
        this.insts = new ArrayList<>();
    }

    //  Main Methods
    public void addInst(Instruction inst){
        this.insts.add(inst);
    }


    //  Getters and Setters
    public void setParentFunc(Function parentFunc) {
        this.parentFunc = parentFunc;
    }
}
