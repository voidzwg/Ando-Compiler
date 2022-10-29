package IR.Value;

import IR.Type.IntegerType;
import IR.Type.LabelType;

import java.util.ArrayList;

public class BasicBlock extends Value{
    private Function parentFunc;
    private ArrayList<BasicBlock> preBlocks;
    private ArrayList<BasicBlock> nxtBlocks;
    private ArrayList<Instruction> insts;
    private boolean isTerminal;
    private int loopDepth;
    private int domLV;
    public static int blockNum = 0;

    public BasicBlock(){
        super("block" + ++blockNum, new LabelType());
        this.insts = new ArrayList<>();
        this.isTerminal = false;
        this.loopDepth = 0;
        this.preBlocks = new ArrayList<>();
        this.nxtBlocks = new ArrayList<>();
    }

    public BasicBlock(Function function){
        super("block" + ++blockNum, new LabelType());
        this.insts = new ArrayList<>();
        this.parentFunc = function;
        this.isTerminal = false;
        this.loopDepth = 0;
        this.preBlocks = new ArrayList<>();
        this.nxtBlocks = new ArrayList<>();
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

    public ArrayList<BasicBlock> getPreBlocks() {
        return preBlocks;
    }

    public ArrayList<BasicBlock> getNxtBlocks() {
        return nxtBlocks;
    }

    public void setPreBlock(BasicBlock bb){
        preBlocks.add(bb);
    }
    public void setNxtBlock(BasicBlock bb){
        nxtBlocks.add(bb);
    }

    public void setDomLV(int domLV){
        this.domLV = domLV;
    }

    public void setLoopDepth(int loopDepth){
        this.loopDepth = loopDepth;
    }

    public int getLoopDepth(){
        return loopDepth;
    }

    @Override
    public String toString(){
        return this.getName();
    }
}
