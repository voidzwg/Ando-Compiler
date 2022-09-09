package IR.Value;

import IR.Type.Type;

import java.util.ArrayList;

public class Function extends Value{
    private ArrayList<BasicBlock> bbs;
    private ArrayList<Argument> args;

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

    public void setBbs(ArrayList<BasicBlock> bbs) {
        this.bbs = bbs;
    }

    public void setArgs(ArrayList<Argument> args) {
        this.args = args;
    }

    public ArrayList<BasicBlock> getBbs() {
        return bbs;
    }

    public ArrayList<Argument> getArgs() {
        return args;
    }
}
