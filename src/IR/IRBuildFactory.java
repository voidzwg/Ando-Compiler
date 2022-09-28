package IR;

import IR.Type.*;
import IR.Value.*;
import IR.Value.Instructions.*;
import com.sun.org.apache.xpath.internal.Arg;

import java.awt.*;
import java.util.ArrayList;

//  PointerType 指 i32*
//  ArrayType 特指指向数组的指针
public class IRBuildFactory {
    private IRBuildFactory(){}

    private static final IRBuildFactory f = new IRBuildFactory();

    public static IRBuildFactory getInstance(){
        return f;
    }

    //  根据dimList递归构建ArrayType
    private Type buildArrayType(ArrayList<Integer> dimList){
        if(dimList.size() == 0){
            return new PointerType(new IntegerType(32));
        }
        ArrayList<Integer> tmpDimList = new ArrayList<>(dimList);
        int eleDim = tmpDimList.get(0);
        tmpDimList.remove(0);
        return new ArrayType(buildArrayType(tmpDimList), eleDim);
    }

    private Type calLowerType(ArrayList<Integer> dimList){
        if(dimList.size() == 1){
            return new PointerType(new IntegerType(32));
        }
        dimList.remove(0);
        return calLowerType(dimList);
    }

    //  Utils方法，用于计算GepInst的Type，保证target一定为一个指针
    private Type calGepType(Type tarType, ArrayList<Value> indexs){
        if(indexs.size() == 0) return tarType;
        if(!tarType.isArrayType()){
            return new IntegerType(32);
        }
        ArrayType arrayType = (ArrayType) tarType;
        Type eleType = arrayType.getEleType();
        ArrayList<Value> tmpIndexs = new ArrayList<>(indexs);
        tmpIndexs.remove(0);
        return calGepType(eleType, tmpIndexs);
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

    public LoadInst buildLoadInst(Value pointer, BasicBlock bb){
        //  loadInst的pointer一定是一个指针
        PointerType pointerType = (PointerType) pointer.getType();
        LoadInst loadInst = new LoadInst(pointer, pointerType.getEleType(), bb);
        bb.addInst(loadInst);
        return loadInst;
    }

    public void buildStoreInst(BasicBlock bb, Value value, Value pointer){
        StoreInst storeInst =  new StoreInst(bb, value, pointer);
        bb.addInst(storeInst);
    }

    public AllocInst buildArray(String name, ArrayList<Integer> dimList, BasicBlock bb, boolean isConst){
        ArrayType arrayType = (ArrayType) buildArrayType(dimList);
        AllocInst allocInst = new AllocInst(name, arrayType, bb, isConst);
        bb.addInst(allocInst);
        return allocInst;
    }

    public AllocInst buildAllocInst(String name, Type type,BasicBlock bb, boolean isConst){
        AllocInst allocInst = new AllocInst(name, new PointerType(type), bb, isConst);
        bb.addInst(allocInst);
        return allocInst;
    }

    public GepInst getGepInst(Value target, ArrayList<Value> indexs,BasicBlock bb){
        //  索引的第一个参数不会改变类型
        ArrayList<Value> tmpIndexs = new ArrayList<>(indexs);
        tmpIndexs.remove(0);
        Value.valNumber--;
        return new GepInst(indexs, target, calGepType(target.getType(), tmpIndexs), bb);
    }

    public GepInst buildGepInst(Value target, ArrayList<Value> indexs,BasicBlock bb){
        //  索引的第一个参数不会改变类型
        ArrayList<Value> tmpIndexs = new ArrayList<>(indexs);
        tmpIndexs.remove(0);
        GepInst gepInst = new GepInst(indexs, target, calGepType(target.getType(), tmpIndexs), bb);
        bb.addInst(gepInst);
        return gepInst;
    }

    //  build ret void
    public RetInst buildRetInst(BasicBlock bb){
        Value voidValue = new Value("void", new VoidType());
        RetInst retInst = new RetInst(bb, voidValue);
        bb.addInst(retInst);
        return retInst;
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

    //  数组参数
    public Argument buildArgument(String name, ArrayList<Integer> dimList, Function parentFunc){
        Type type;
        if(dimList.size() == 0) type = new PointerType(new IntegerType(32));
        else type = buildArrayType(dimList);
        Argument argument = new Argument(name, type, parentFunc);
        parentFunc.addArg(argument);
        return argument;
    }

    public Argument buildArgument(String name, String typeStr, Function parentFunc){
        Argument argument;
        if(typeStr.equals("int")) argument = new Argument(name, new IntegerType(32), parentFunc);
        else argument = new Argument(name, new VoidType(), parentFunc);
        parentFunc.addArg(argument);
        return argument;
    }

    //  GlobalVar数组版
    public GlobalVar buildGlobalVar(String name, ArrayList<Integer> dimList, ArrayList<Value> initValS ,boolean isConst, ArrayList<GlobalVar> globalVars){
        ArrayType arrayType = (ArrayType) buildArrayType(dimList);
        GlobalVar globalVar = new GlobalVar(name, arrayType, isConst, initValS);
        globalVars.add(globalVar);
        return globalVar;
    }

    public GlobalVar buildGlobalVar(String name,Type type, boolean isConst, Value initValue,ArrayList<GlobalVar> globalVars){
        GlobalVar globalVar = new GlobalVar(name, type,isConst, initValue);
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
