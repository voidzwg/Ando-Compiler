package Utils;

import IR.IRModule;
import IR.Type.ArrayType;
import IR.Type.PointerType;
import IR.Type.StringType;
import IR.Type.Type;
import IR.Value.*;
import IR.Value.Instructions.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;

public class IRDump {
    private static final BufferedWriter out;

    //  用于辅助输出数组初始值的小变量qwq
    private static int initArrayNow;

    static {
        try {
            out = new BufferedWriter(new FileWriter(Global.outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //  将rawFString转换成FString
    private static String calFString(String rawFString){
        String fString = rawFString.replace("\"", "").replace("\\n", "\\0A");

        return fString + "\\00";
    }

    private static int calFStrLen(String fString){
        int len = fString.length();
        int realLen = len;
        for(int i = 0; i < len; i++){
            if(fString.charAt(i) == '\\'){
                realLen-=2;
            }
        }
        return realLen;
    }

    private static void DumpType(Type type) throws IOException {
        if(type instanceof ArrayType){
            ArrayType arrayType = (ArrayType) type;
            Type eleType = arrayType.getEleType();
            int len = arrayType.getEleDim();
            if(!eleType.isArrayType()){
                out.write("[" + len + " x i32]");
            }
            else{
                out.write("[" + len + " x ");
                DumpType(eleType);
                out.write("]");
            }
        }
        else if(type instanceof PointerType){
            out.write("i32*");
        }
    }

    private static void DumpInitArray(ArrayType arrayType, ArrayList<Value> values) throws IOException {
        Type type = arrayType.getEleType();
        DumpType(arrayType);
        if(type.isArrayType()){
            ArrayType sonType = (ArrayType) type;
            int len = arrayType.getEleDim();
            out.write("[");
            for(int i = 0; i < len; i++){
                DumpInitArray(sonType, values);
                if(i != len - 1) out.write(", ");
            }
            out.write("]");
        }
        else if(type.isPointerType()){
            out.write(" ");
            out.write("[");
            int len = arrayType.getEleDim();
            for(int i = 0; i < len;i++){
                out.write("i32 " + values.get(initArrayNow++).getName());
                if(i != len - 1) out.write(", ");
            }
            out.write("]");
        }
    }

    private static void DumpGlobalVar(GlobalVar globalVar) throws IOException {
        if(globalVar.getType().isArrayType()){
            out.write(globalVar.getName());

            if(globalVar.isConst()){
                out.write(" = constant ");
            }
            else out.write(" = global ");

            ArrayType arrayType = (ArrayType) globalVar.getType();
            //  输出初始值
            ArrayList<Value> values = globalVar.getValues();
            if(values.size() == 0){
                out.write(" zeroinitializer\n");
            }
            else {
                //  初始化当前输出的位置
                initArrayNow = 0;
                DumpInitArray(arrayType, values);
            }
        }
        //  为printf贴心设计
        else if(globalVar.getType() instanceof StringType){
            String strName = globalVar.getName();
            StringType stringType = (StringType) globalVar.getType();
            //  由于fString中可能由\n等字符，所以要先预处理一下
            String fString = calFString(stringType.getVal());
            int len = calFStrLen(fString);

            out.write(strName + " = constant ");
            out.write("[" + len + " x i8] c");
            out.write("\"" + fString + "\"");
        }
        else {
            out.write(globalVar.getName() + " = global i32 ");
            out.write(globalVar.getValue().getName());
        }
    }

    private static void DumpLib() throws IOException {
        out.write("declare i32 @getint()\n");
        out.write("declare void @memset(i32*, i32, i32)\n");
        out.write("declare i32 @printf(i8*, ...)\n");

    }
    public static void DumpModule(IRModule module) throws IOException {
        DumpLib();
        //  DumpGlobalVars
        ArrayList<GlobalVar> globalVars = module.getGlobalVars();
        for(GlobalVar globalVar : globalVars){
            if(!globalVar.isConst() || globalVar.getType().isArrayType()){
                DumpGlobalVar(globalVar);
                out.write("\n");
            }
        }

        //  DumpFunctions
        ArrayList<Function> functions = module.getFunctions();
        for (Function function : functions) {
            DumpFunction(function);
            out.write("\n");
        }
        out.close();
    }

    private static void DumpArgument(Argument argument) throws IOException {
        if(argument.getType().isIntegerTy()){
            out.write("i32 ");
            out.write(argument.getName());
        }
        else if(argument.getType().isArrayType()){
            ArrayType arrayType = (ArrayType) argument.getType();
            DumpType(arrayType);
            out.write("* " + argument.getName());
        }
    }

    private static void DumpFunction(Function function) throws IOException {
        out.write("define ");
        if(function.getType().isIntegerTy()){
            out.write("i32 ");
        }
        else out.write("void ");
        out.write(function.getName() + "(");

        ArrayList<Argument> arguments = function.getArgs();
        for(int i = 0; i < arguments.size(); i++){
            Argument argument = arguments.get(i);
            DumpArgument(argument);
            if(i != arguments.size() - 1) out.write(", ");
        }

        out.write(") {\n");

        ArrayList<BasicBlock> basicBlocks = function.getBbs();
        for(BasicBlock block : basicBlocks){
            DumpBasicBlock(block);
        }

        out.write("}\n");
    }

    private static void DumpBasicBlock(BasicBlock bb) throws IOException {
        out.write(bb.getName() + ":\n");
        ArrayList<Instruction> insts = bb.getInsts();
        for(Instruction inst : insts){
            out.write("\t");
            DumpInstruction(inst);
        }
    }

    private static void DumpInstruction(Instruction inst) throws IOException {
        if(inst instanceof RetInst){
            RetInst retInst = (RetInst) inst;
            out.write("ret ");

            Value value = retInst.getValue();
            if(value.getType().isIntegerTy()) out.write("i32 ");

            if(value instanceof ConstInteger) {
                ConstInteger intConst = (ConstInteger) value;
                out.write(String.valueOf(intConst.getVal()));
            }

            else out.write(value.getName());

            out.write("\n");
        }

        else if(inst instanceof BinaryInst){
            BinaryInst binaryInst = (BinaryInst) inst;
            Value left = binaryInst.getLeftVal();
            Value right = binaryInst.getRightVal();
            OP op = binaryInst.getOp();

            if(op == OP.Add){
                out.write(inst.getName() + " = add i32 ");
            }
            else if(op == OP.Sub){
                out.write(inst.getName() + " = sub i32 ");
            }
            else if(op == OP.Eq){
                out.write(inst.getName() + " = eq i32 ");
            }
            else if(op == OP.Mul){
                out.write(inst.getName() + " = mul i32 ");
            }
            else if(op == OP.Div){
                out.write(inst.getName() + " = div i32 ");
            }
            else if(op == OP.Mod){
                out.write(inst.getName() + " = mod i32 ");
            }

            out.write(left.getName() + ", ");
            out.write(right.getName() + "\n");
        }

        else if(inst instanceof LoadInst){
            LoadInst loadInst = (LoadInst) inst;
            Value pointer = loadInst.getPointer();
            if(pointer.getType().isArrayType()){
                ArrayType arrayType = (ArrayType) pointer.getType();
                out.write(inst.getName() + " = load ");
                DumpType(arrayType.getEleType());
                DumpType(arrayType);
            }
            else {
                out.write(inst.getName() + " = load i32, i32* ");
            }
            out.write(pointer.getName() + "\n");
        }

        else if(inst instanceof AllocInst){
            if(!inst.getType().isArrayType()) {
                out.write(inst.getName() + " = alloca i32\n");
            }
            //  数组
            else {
                ArrayType arrayType = (ArrayType) inst.getType();
                out.write(inst.getName() + " = alloca ");
                DumpType(arrayType);
                out.write("\n");
            }

        }

        else if(inst instanceof StoreInst){
            StoreInst storeInst = (StoreInst) inst;
            out.write("store i32 ");
            out.write(storeInst.getValue().getName() + ", i32* ");
            out.write(storeInst.getPointer().getName() + "\n");
        }

        else if(inst instanceof CmpInst){
            CmpInst cmpInst = (CmpInst) inst;
            OP op = cmpInst.getOp();
            out.write(cmpInst.getName() + " = icmp ");
            if(op == OP.Eq) out.write("eq");
            else if(op == OP.Ne) out.write("ne");
            else if(op == OP.Gt) out.write("sgt");
            else if(op == OP.Ge) out.write("sge");
            else if(op == OP.Lt) out.write("slt");
            else if(op == OP.Le) out.write("sle");

            Value left = cmpInst.getLeftVal();
            Value right = cmpInst.getRightVal();
            out.write(" " + left + ",");
            out.write(" " + right + "\n");
        }

        else if(inst instanceof BrInst){
            BrInst brInst = (BrInst) inst;
            if(brInst.getJumType() == 1) {
                out.write("br i1 ");
                out.write(brInst.getJudVal().getName() + ", ");
                out.write("label %" + brInst.getLabelLeft().getName() + ", ");
                out.write("label %" + brInst.getLabelRight().getName() + "\n");
            }
            //  直接跳转
            else {
                out.write("br label %");
                out.write(brInst.getLabelJump().getName() + "\n");
            }
        }

        else if(inst instanceof CallInst){
            CallInst callInst = (CallInst) inst;
            String FuncName = callInst.getCallFunc().getName();
            if(!callInst.getType().isVoidTy()){
                out.write(callInst.getName() + " = ");
            }

            if(callInst.getType().isVoidTy()) out.write("call void ");
            else if(callInst.getType().isIntegerTy()) out.write("call i32 ");

            //  特殊的printf
            if(FuncName.equals("@printf")){
                out.write("(i8*, ...) ");
            }

            out.write(FuncName);
            out.write("(");

            ArrayList<Value> values = callInst.getValues();

            //  插播一段对printf的特殊报告
            if(FuncName.equals("@printf")) {
                Value strVal = values.get(0);
                values.remove(0);
                StringType stringType = (StringType) strVal.getType();
                String fString = calFString(stringType.getVal());
                int len = calFStrLen(fString);

                out.write("i8* getelementptr (");
                out.write("[" + len + " x i8], ");
                out.write("[" + len + " x i8]* ");
                out.write(strVal.getName() + ", i64 0, i64 0)");

                if(values.size() != 0) out.write(", ");
            }

            for(int i = 0; i < values.size(); i++){
                Value value = values.get(i);
                out.write(value.toString());
                if(i != values.size() - 1) out.write(", ");
            }

            out.write(")\n");
        }

        else if(inst instanceof GepInst){
            GepInst gepInst = (GepInst) inst;
            Value target = gepInst.getTarget();
            Type gepType = gepInst.getType();
            Type tarType = target.getType();

            if(tarType instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) tarType;
                out.write(gepInst.getName() + " = getelementptr ");
                DumpType(arrayType);
                out.write(", ");
                DumpType(arrayType);
                out.write("* " + target.getName());
            }

            else if(tarType instanceof PointerType){
                out.write(gepInst.getName() + " = getelementptr i32 i32* ");
                out.write(target.getName());
            }

            ArrayList<Value> indexs = gepInst.getIndexs();
            for(Value index : indexs){
                out.write(", i32 " + index.getName());
            }

            out.write("\n");
        }
    }
}
