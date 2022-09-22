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

    public void buildBrInst(BasicBlock jumpBB, BasicBlock basicBlock){
        BrInst brInst = new BrInst(jumpBB, basicBlock);
        basicBlock.addInst(brInst);
        basicBlock.setTerminal(true);
    }

    public void buildBrInst(Value judVal, BasicBlock left, BasicBlock right, BasicBlock cur){
        BrInst brInst = new BrInst(judVal, left, right, cur);
        cur.addInst(brInst);
        cur.setTerminal(true);
    }

    public LoadInst buildLoadInst(Value pointer,BasicBlock bb){
        LoadInst loadInst = new LoadInst(pointer, bb);
        bb.addInst(loadInst);
        return loadInst;
    }

    public void buildStoreInst(BasicBlock bb, Value value, Value pointer){
        StoreInst storeInst =  new StoreInst(bb, value, pointer);
        bb.addInst(storeInst);
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

    public CallInst buildCallInst(BasicBlock bb, Function callFunc, ArrayList<Value> values){
        CallInst callInst = new CallInst(bb, callFunc, values);
        bb.addInst(callInst);
        return callInst;
    }
    public CallInst buildCallInst(BasicBlock bb, Function callFunc){
        CallInst callInst = new CallInst(bb, callFunc);
        bb.addInst(callInst);
        return callInst;
    }

    public BasicBlock buildBasicBlock(Function CurFunction){
        BasicBlock bb = new BasicBlock(CurFunction);
        CurFunction.getBbs().add(bb);
        return bb;
    }

    public Argument buildArgument(String name, String type, Function parentFunc){
        Argument argument;
        if(type.equals("int")){
            argument = new Argument(name, new IntegerType(32), parentFunc);
        }
        else {
            argument = new Argument(name, new VoidType(), parentFunc);
        }
        parentFunc.addArg(argument);
        return argument;
    }

    public GlobalVar buildGlobalVar(String name, boolean isConst, Value initValue,ArrayList<GlobalVar> globalVars){
        GlobalVar globalVar = new GlobalVar(name, isConst, initValue);
        globalVars.add(globalVar);
        return globalVar;
    }
    public Function buildFunction(String name, String type, IRModule module){

        Function function;
        if(type.equals("int")){
            function = new Function(name, new IntegerType(32));
        }
        else {
            function = new Function(name, new VoidType());
        }
        module.addFunction(function);
        return function;
    }
}
