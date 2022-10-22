package Utils;

import Backend.MCModule;
import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCData;
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
        //  输出data段
        ArrayList<MCData> mcDatas = mcModule.getData();
        if(mcDatas.size() != 0) out.write(".data\n");
        for(MCData mcData : mcDatas){
            if(mcData.getType() == 0){
                out.write("\t" + mcData.getName() + ": .asciiz\"" + mcData.getString() + "\"\n");
            }
        }

        //  输出text段
        out.write(".text\n");

        ArrayList<MCFunction> mcFunctions = mcModule.getMcFunctions();
        //  先输出main函数, 后面爱咋输出咋输出qwq
        for(MCFunction mcFunction : mcFunctions){
            if(mcFunction.getName().equals("main")) {
                DumpMCFunction(mcFunction);
            }
        }
        for(MCFunction mcFunction : mcFunctions){
            if(!mcFunction.getName().equals("main")) {
                DumpMCFunction(mcFunction);
            }
        }

        out.close();
    }
}
