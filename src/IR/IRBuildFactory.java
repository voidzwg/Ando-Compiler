package IR;

import IR.Type.IntegerType;
import IR.Type.VoidType;
import IR.Value.*;
import IR.Value.Instructions.*;

import java.util.ArrayList;

public class IRBuildFactory {
    private IRBuildFactory(){}

    private static final IRBuildFactory f = new IRBuildFactory();

    public static IRBuildFactory getInstance(){
        return f;
    }

    public BinaryInst buildBinaryInst(OP op, Value left, Value right,BasicBlock bb){
        BinaryInst binaryInst = new BinaryInst(op, left, right, bb);
        bb.addInst(binaryInst);
        return binaryInst;
    }

    public Value buildNumber(int val){
        return new ConstInteger(val);
    }

    public CmpInst buildCmpInst(Value left, Value right, OP op, BasicBlock bb){
        CmpInst cmpInst = new CmpInst(left, right, op, bb);
        bb.addInst(cmpInst);
        return cmpInst;
    }

    public BrInst buildBrInst(Value judVal, BasicBlock left, BasicBlock right, BasicBlock cur){
        BrInst brInst = new BrInst(judVal, left, right, cur);
        cur.addInst(brInst);
        return brInst;
    }

    public LoadInst buildLoadInst(Value pointer,BasicBlock bb){
        LoadInst loadInst = new LoadInst(pointer, bb);
        bb.addInst(loadInst);
        return loadInst;
    }

    public StoreInst buildStoreInst(BasicBlock bb, Value value, Value pointer){
        StoreInst storeInst =  new StoreInst(bb, value, pointer);
        bb.addInst(storeInst);
        return storeInst;
    }

    public AllocInst buildAllocInst(String name, BasicBlock bb){
        AllocInst allocInst = new AllocInst(name, new IntegerType(32), bb);
        bb.addInst(allocInst);
        return allocInst;
    }

    public RetInst buildRetInst(BasicBlock bb, Value value){
        RetInst retInst = new RetInst(bb, value);
        bb.addInst(retInst);
        return retInst;
    }

    public BasicBlock buildBasicBlock(Function CurFunction){
        BasicBlock bb = new BasicBlock(CurFunction);
        CurFunction.getBbs().add(bb);
        return bb;
    }

    public Function buildFunction(String name, String type){

        if(type.equals("int")) return new Function(name, new IntegerType(32));
        else return new Function(name, new VoidType());
    }

    public IRModule buildModule(ArrayList<Function> functions, ArrayList<GlobalVars> globalVars) {
        return new IRModule(functions, globalVars);
    }
}
