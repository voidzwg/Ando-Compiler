package Utils;

import IR.IRModule;
import IR.Value.*;
import IR.Value.Instructions.BinaryInst;
import IR.Value.Instructions.RetInst;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MIPSDump {
    static BufferedWriter out;

    static {
        try {
            out = new BufferedWriter(new FileWriter(Global.outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void DumpMips(IRModule module) throws IOException {

        out.write("\t.text\n");
        out.write("\t.globl main\n");

        ArrayList<Function> functions = module.getFunctions();
        for(Function function : functions){
            ArrayList<BasicBlock> basicBlocks = function.getBbs();
            for(BasicBlock basicBlock : basicBlocks){
                DumpBasicBlock(basicBlock);
            }
        }

        out.close();
    }

    private static void DumpBasicBlock(BasicBlock basicBlock) throws IOException {
        String bbName = basicBlock.getName();
        if(bbName.equals("block1")) bbName = "main";

        out.write(bbName + ":\n");

        ArrayList<Instruction> insts = basicBlock.getInsts();
        for(Instruction inst : insts){
            DumpInst(inst);
        }
    }

    private static void DumpInst(Instruction inst) throws IOException {
        if(inst instanceof RetInst){
            Value value = ((RetInst) inst).getValue();

            out.write("\t");
            out.write("li a0, " + value.getName() + "\n");
            out.write("\t");
            out.write("ret\n");
        }
        else if(inst instanceof BinaryInst){

        }
    }
}
