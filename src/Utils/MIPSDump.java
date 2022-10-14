package Utils;

import Backend.MCModule;
import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Backend.MachineValue.MachineInst.MCInst;

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

    private static void DumpMCInst(MCInst mcInst) throws IOException {
        out.write("\t" + mcInst + "\n");
    }

    private static void DumpMCBlock(MCBlock mcBlock) throws IOException {
        out.write(mcBlock.getName() + ":\n");
        ArrayList<MCInst> mcInsts = mcBlock.getMCInsts();
        for(MCInst mcInst : mcInsts){
            DumpMCInst(mcInst);
        }

    }

    private static void DumpMCFunction(MCFunction mcFunction) throws IOException {
        ArrayList<MCBlock> mcBlocks = mcFunction.getMcBlocks();

        for(MCBlock mcBlock : mcBlocks){
            DumpMCBlock(mcBlock);
        }
    }

    public static void DumpMCModule(MCModule mcModule) throws IOException {
        out.write("\t.text\n");
        out.write("\t.globl main_0\n");

        ArrayList<MCFunction> mcFunctions = mcModule.getMcFunctions();
        for(MCFunction mcFunction : mcFunctions){
            DumpMCFunction(mcFunction);
        }

        out.close();
    }
}
