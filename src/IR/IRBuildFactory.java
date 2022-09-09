package IR;

import IR.Type.IntegerType;
import IR.Type.VoidType;
import IR.Value.*;
import IR.Value.Instructions.RetInst;

import java.util.ArrayList;

public class IRBuildFactory {
    private IRBuildFactory(){}

    private static final IRBuildFactory f = new IRBuildFactory();

    public static IRBuildFactory getInstance(){
        return f;
    }

    public Value buildNumber(int val){
        return new ConstInteger(val);
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
