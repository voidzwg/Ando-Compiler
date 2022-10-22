package IR.Value;

import IR.Type.IntegerType;
import IR.Type.LabelType;

import java.util.ArrayList;

public class BasicBlock extends Value{
    private Function parentFunc;
    private ArrayList<Instruction> insts;
    private boolean isTerminal;
    private int loopDepth;

    public static int blockNum = 0;

    public BasicBlock(){
        super("block" + ++blockNum, new LabelType());
        this.insts = new ArrayList<>();
        this.isTerminal = false;
        this.loopDepth = 0;
    }

    public BasicBlock(Function function){
        super("block" + ++blockNum, new LabelType());
        this.insts = new ArrayList<>();
        this.parentFunc = function;
        this.isTerminal = false;
        this.loopDepth = 0;
    }

    //  Main Methods
    public void addInst(Instruction inst){
        this.insts.add(inst);
    }


    public ArrayList<Instruction> getInsts() {
        return insts;
    }

    public void setTerminal(boolean terminal) {
        isTerminal = terminal;
    }

    public void removeInst(Instruction instruction){
        insts.remove(instruction);
    }

    public void setLoopDepth(int loopDepth){
        this.loopDepth = loopDepth;
    }

    public int getLoopDepth(){
        return loopDepth;
    }
}
