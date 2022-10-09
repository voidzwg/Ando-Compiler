package Backend;

import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Backend.MachineValue.MachineInst.*;
import Backend.Reg.VirtualReg;
import IR.IRModule;
import IR.Value.*;
import IR.Value.Instructions.BinaryInst;
import IR.Value.Instructions.OP;
import IR.Value.Instructions.RetInst;
import Utils.Global;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MCModule {

    //  虚拟寄存器$0表示x0，$1表示a0
    private ArrayList<MCFunction> mcFunctions = new ArrayList<>();
    private MCFunction CurFunction;
    private MCBlock CurBlock;

    //  valRegMap用于存储VirReg中的数据
    private HashMap<Integer, VirtualReg> valRegMap = new HashMap<>();
    //  regMap用于存储Value到VirReg的映射
    private HashMap<String, VirtualReg> regMap = new HashMap<>();

    private VirtualReg valueToReg(Value value){
        String ident = value.getName();
        if(regMap.containsKey(ident)){
            return regMap.get(ident);
        }
        if(value instanceof ConstInteger){
            int num = Integer.parseInt(ident);
            if(valRegMap.containsKey(num)){
                return valRegMap.get(num);
            }

            VirtualReg virtualReg;
            if(num == 0) virtualReg = new VirtualReg(0);
            else virtualReg = new VirtualReg();
            valRegMap.put(num, virtualReg);
            regMap.put(ident, virtualReg);
            CurBlock.addInst(new MCLi(virtualReg, num));
            return virtualReg;
        }
        else{
            VirtualReg virtualReg = new VirtualReg();
            regMap.put(ident, virtualReg);
            return virtualReg;
        }
    }

    private MCInst.Tag OPToTag(OP op){
        if(op == OP.Sub) return MCInst.Tag.sub;
        else if(op == OP.Add) return MCInst.Tag.add;
        else return null;
    }

    //  返回加载进的VirReg
    private VirtualReg buildMCLi(int val){
        VirtualReg virtualReg;
        if(!valRegMap.containsKey(val)){
            virtualReg = new VirtualReg();
            valRegMap.put(val, virtualReg);
        }
        else virtualReg = valRegMap.get(val);
        CurBlock.addInst(new MCLi(virtualReg, val));
        return virtualReg;
    }

    private void genInst(Instruction instruction) throws IOException {
        if(instruction instanceof RetInst){
            RetInst retInst = (RetInst) instruction;
            if(retInst.getValue() instanceof ConstInteger) {
                ConstInteger intConst = (ConstInteger) retInst.getValue();
                int imm = intConst.getVal();

                CurBlock.addInst(new MCLi(new VirtualReg(1), imm));
            }
            else{
                VirtualReg virtualReg = valueToReg(retInst.getValue());
                CurBlock.addInst(new MCMv(new VirtualReg(1), virtualReg));
            }
            CurBlock.addInst(new MCReturn());
        }
        else if(instruction instanceof BinaryInst){
            BinaryInst binaryInst = (BinaryInst) instruction;
            OP op = binaryInst.getOp();
            Value left = binaryInst.getLeftVal();
            Value right = binaryInst.getRightVal();
            //  tmpLeft，tmpRight用于只有一个是ConstInteger的情况
            //  tmpLeft用来存AllocInst, tmpRight用来存ConstInteger
            Value tmpLeft = left;
            Value tmpRight = right;
            int isImm = 0;

            if(left instanceof ConstInteger){
                isImm++;
                tmpLeft = right;
                tmpRight = left;
            }
            if(right instanceof ConstInteger){
                isImm++;
                tmpLeft = left;
                tmpRight = right;
            }

            if(op == OP.Eq){
                if(isImm == 2){
                    int leftVal = ((ConstInteger) left).getVal();
                    int rightVal = ((ConstInteger) right).getVal();
                    int ans;
                    if(leftVal == rightVal) ans = 1;
                    else ans = 0;
                    VirtualReg virtualReg = buildMCLi(ans);
                    regMap.put(binaryInst.getName(), virtualReg);
                }
                else if(isImm == 1){

                }
            }
            //  Add, Sub
            else{
                //  全是constInteger，我他妈直接运算
                if(isImm == 2){
                    int leftVal = ((ConstInteger) left).getVal();
                    int rightVal = ((ConstInteger) right).getVal();
                    if(op == OP.Add) buildMCLi(leftVal + rightVal);
                    else if(op == OP.Sub) buildMCLi(leftVal - rightVal);
                }

                else if(isImm == 1){
                    ConstInteger constInteger = (ConstInteger) tmpRight;
                    int val = constInteger.getVal();
                    VirtualReg rs1 = valueToReg(tmpLeft);
                    VirtualReg rd = valueToReg(binaryInst);
                    if(op == OP.Sub) val = -val;
                    CurBlock.addInst(new MCBinaryInst(MCInst.Tag.addi, rd, rs1, val));
                }

                else {
                    VirtualReg rs1 = valueToReg(left);
                    VirtualReg rs2 = valueToReg(right);
                    VirtualReg rd = valueToReg(binaryInst);
                    MCInst.Tag tag = OPToTag(op);
                    CurBlock.addInst(new MCBinaryInst(tag, rd, rs1, rs2));
                }
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
