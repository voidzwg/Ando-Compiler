package IR.Value;

import IR.Type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Function extends Value{
    private final ArrayList<BasicBlock> bbs;
    private final ArrayList<Argument> args;
    private HashMap<BasicBlock, ArrayList<BasicBlock>> idoms;
    private BasicBlock bbEntry;

    public Function(String name, Type type){
        super(name, type);
        this.bbs = new ArrayList<>();
        this.args = new ArrayList<>();
    }

    public Function(String name, Type type, ArrayList<BasicBlock> bbs, ArrayList<Argument> args){
        super(name, type);
        this.bbs = bbs;
        this.args = args;
    }

    public void addArg(Argument argument){ args.add(argument); }

    public ArrayList<BasicBlock> getBbs() {
        return bbs;
    }

    public ArrayList<Argument> getArgs() {
        return args;
    }

    public void setIdoms(HashMap<BasicBlock, ArrayList<BasicBlock>> idoms) {
        this.idoms = idoms;
    }

    public void setBbEntry(BasicBlock bbEntry) {
        this.bbEntry = bbEntry;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getIdoms() {
        return idoms;
    }

    public BasicBlock getBbEntry() {
        return bbEntry;
    }
}
