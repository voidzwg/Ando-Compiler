package Utils;

import IR.IRModule;
import IR.Value.*;
import IR.Value.Instructions.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class IRDump {
    private static final BufferedWriter out;

    static {
        try {
            out = new BufferedWriter(new FileWriter(Global.outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void DumpModule(IRModule module) throws IOException {
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
            out.write(inst.getName() + " = load i32, i32* ");
            out.write(((LoadInst) inst).getPointer().getName() + "\n");
        }

        else if(inst instanceof AllocInst){
            out.write(inst.getName() + " = alloc i32\n");
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

            out.write(" " + cmpInst.getLeftVal().getName());
            out.write(" " + cmpInst.getRightVal().getName() + " " + "\n");
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
            if(!callInst.getType().isVoidTy()){
                out.write(callInst.getName() + " = ");
            }

            if(callInst.getType().isVoidTy()) out.write("call void ");
            else if(callInst.getType().isIntegerTy()) out.write("call i32 ");

            out.write(callInst.getCallFunc().getName());
            out.write("(");

            ArrayList<Value> values = callInst.getValues();
            for(int i = 0; i < values.size(); i++){
                Value value = values.get(i);
                if(value.getType().isIntegerTy()){
                    out.write("i32 ");
                }
                out.write(value.getName());
                if(i != values.size() - 1) out.write(", ");
            }

            out.write(")\n");
        }
    }
}
