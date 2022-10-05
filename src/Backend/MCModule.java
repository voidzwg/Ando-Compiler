package Backend;

import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Backend.MachineValue.MachineInst.MCInst;
import Backend.MachineValue.MachineInst.MCLi;
import Backend.MachineValue.MachineInst.MCReturn;
import IR.IRModule;
import IR.Value.BasicBlock;
import IR.Value.ConstInteger;
import IR.Value.Function;
import IR.Value.Instruction;
import IR.Value.Instructions.RetInst;
import Utils.Global;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MCModule {


    private ArrayList<MCFunction> mcFunctions = new ArrayList<>();
    private MCFunction CurFunction;
    private MCBlock CurBlock;

    private void genInst(Instruction instruction) throws IOException {
        if(instruction instanceof RetInst){
            RetInst retInst = (RetInst) instruction;
            if(retInst.getValue() instanceof ConstInteger) {
                ConstInteger intConst = (ConstInteger) retInst.getValue();
                int imm = intConst.getVal();

                CurBlock.addInst(new MCLi(imm));
                CurBlock.addInst(new MCReturn());
            }
        }
    }

    private void genBasicBlock(BasicBlock basicBlock) throws IOException {
        ArrayList<Instruction> instructions = basicBlock.getInsts();
        ArrayList<MCInst> mcInsts = new ArrayList<>();
        CurBlock = new MCBlock(CurFunction, mcInsts,basicBlock.getName());
        for(Instruction instruction : instructions){
            genInst(instruction);
        }

    }
    private void genFunction(Function function) throws IOException {
        String name = function.getName();
        name = name.replace("@", "");

        ArrayList<MCBlock> mcBlocks = new ArrayList<>();
        ArrayList<BasicBlock> basicBlocks = function.getBbs();
        for(BasicBlock basicBlock : basicBlocks){
            genBasicBlock(basicBlock);
            mcBlocks.add(CurBlock);
        }

        CurFunction = new MCFunction(name, mcBlocks);
        mcFunctions.add(CurFunction);
    }

    public void genMips(IRModule irModule) throws IOException {
        ArrayList<Function> functions = irModule.getFunctions();
        for(Function function : functions){
            genFunction(function);
        }
    }

    public ArrayList<MCFunction> getMcFunctions() {
        return mcFunctions;
    }
}
