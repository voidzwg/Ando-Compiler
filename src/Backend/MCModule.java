package Backend;

import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Backend.MachineValue.MachineInst.*;
import Backend.Reg.PhysicalReg;
import Backend.Reg.Reg;
import Backend.Reg.VirtualReg;
import IR.IRModule;
import IR.Value.*;
import IR.Value.Instructions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MCModule {

    //  虚拟寄存器$0表示x0，$1表示a0
    private final ArrayList<MCFunction> mcFunctions = new ArrayList<>();
    private MCFunction CurFunction;
    private MCBlock CurBlock;
    private int CurSize;

    //  valRegMap用于存储VirReg中的数据
    private final HashMap<Integer, Reg> valRegMap = new HashMap<>();
    //  regMap用于存储Value到VirReg的映射
    private final HashMap<String, Reg> regMap = new HashMap<>();
    //  spMap存储value到sp中的位置
    private final HashMap<String, Integer> spMap = new HashMap<>();
    private int CurSpTop = 0;


    private int calSize(Function function){
        int size = 0;
        ArrayList<BasicBlock> bbs = function.getBbs();
        for(BasicBlock bb : bbs){
            ArrayList<Instruction> insts = bb.getInsts();
            for(Instruction inst : insts){
                if(inst.hasName()){
                    size += 4;
                }
            }
        }
        return (size + 15) / 16 * 16;
    }

    private Reg valueToReg(Value value){
        String ident = value.getName();
        if(regMap.containsKey(ident)){
            return regMap.get(ident);
        }
        if(value instanceof ConstInteger){
            int num = Integer.parseInt(ident);
            if(valRegMap.containsKey(num)){
                return valRegMap.get(num);
            }

            Reg reg;
            if(num == 0) reg = PhysicalReg.x0;
            else reg = new VirtualReg();
            valRegMap.put(num, reg);
            regMap.put(ident, reg);
            CurBlock.addInst(new MCLI(reg, num));
            return reg;
        }
        else{
            Reg reg = new VirtualReg();
            regMap.put(ident, reg);
            return reg;
        }
    }

    private MCInst.Tag OPToTag(OP op){
        if(op == OP.Sub) return MCInst.Tag.sub;
        else if(op == OP.Add) return MCInst.Tag.add;
        else if(op == OP.Mul) return MCInst.Tag.mul;
        else if(op == OP.Div) return MCInst.Tag.div;
        else return null;
    }

    //  返回加载进的VirReg
    private Reg buildMCLi(int val){
        Reg reg;
        if(!valRegMap.containsKey(val)){
            reg = new VirtualReg();
            valRegMap.put(val, reg);
        }
        else reg = valRegMap.get(val);
        CurBlock.addInst(new MCLI(reg, val));
        return reg;
    }

    private void genInst(Instruction instruction){
        if(instruction instanceof RetInst){
            RetInst retInst = (RetInst) instruction;
            if(retInst.getValue() instanceof ConstInteger) {
                ConstInteger intConst = (ConstInteger) retInst.getValue();
                int imm = intConst.getVal();

                CurBlock.addInst(new MCLI(PhysicalReg.a0, imm));
            }
            else{
                Reg reg = valueToReg(retInst.getValue());
                CurBlock.addInst(new MCMV(PhysicalReg.a0, reg));
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
                    Reg reg = buildMCLi(ans);
                    regMap.put(binaryInst.getName(), reg);
                }
                else if(isImm == 1){
                    int imm = ((ConstInteger) tmpRight).getVal();
                    Reg rs1 = valueToReg(tmpLeft);
                    Reg res = new VirtualReg();
                    CurBlock.addInst(new MCBinaryInst(MCInst.Tag.xori, res, rs1, imm));

                    Reg rd = valueToReg(binaryInst);
                    CurBlock.addInst(new MCBinaryInst(MCInst.Tag.seq, rd, res));
                }
                else{
                    Reg rs1 = valueToReg(left);
                    Reg rs2 = valueToReg(right);
                    Reg res = new VirtualReg();
                    CurBlock.addInst(new MCBinaryInst(MCInst.Tag.xor, res, rs1, rs2));

                    Reg rd = valueToReg(binaryInst);
                    CurBlock.addInst(new MCBinaryInst(MCInst.Tag.seq, rd, res));
                }
            }
            //  add, sub, mul, div
            else{
                //  全是constInteger，我他妈直接运算
                if(isImm == 2){
                    int leftVal = ((ConstInteger) left).getVal();
                    int rightVal = ((ConstInteger) right).getVal();
                    if(op == OP.Add) buildMCLi(leftVal + rightVal);
                    else if(op == OP.Sub) buildMCLi(leftVal - rightVal);
                    else if(op == OP.Mul) buildMCLi(leftVal * rightVal);
                    else if(op == OP.Div) buildMCLi(leftVal / rightVal);
                }

                else if(isImm == 1 && (op == OP.Sub || op == OP.Add)){
                    ConstInteger constInteger = (ConstInteger) tmpRight;
                    int val = constInteger.getVal();
                    Reg rs1 = valueToReg(tmpLeft);
                    Reg rd = valueToReg(binaryInst);
                    if(op == OP.Sub) val = -val;
                    CurBlock.addInst(new MCBinaryInst(MCInst.Tag.addi, rd, rs1, val));
                }

                else {
                    Reg rs1 = valueToReg(left);
                    Reg rs2 = valueToReg(right);
                    Reg rd = valueToReg(binaryInst);
                    MCInst.Tag tag = OPToTag(op);
                    CurBlock.addInst(new MCBinaryInst(tag, rd, rs1, rs2));
                }
            }

        }
        else if(instruction instanceof StoreInst){
            StoreInst storeInst = (StoreInst) instruction;
            Reg value = valueToReg(storeInst.getValue());
            int offset = CurSpTop;
            Value pointer = storeInst.getPointer();
            if(spMap.containsKey(pointer.getName())){
                offset = spMap.get(pointer.getName());
            }
            else {
                spMap.put(pointer.getName(), offset);
                CurSpTop += 4;
            }
            CurBlock.addInst(new MCSW(value, PhysicalReg.sp, offset));
        }
        else if(instruction instanceof LoadInst){
            LoadInst loadInst = (LoadInst) instruction;
            int offset = spMap.get(loadInst.getPointer().getName());
            Reg rs = valueToReg(loadInst);
            CurBlock.addInst(new MCLoad(PhysicalReg.sp, rs, offset));
        }
    }

    private void genBasicBlock(BasicBlock basicBlock, boolean isEntry){
        ArrayList<Instruction> instructions = basicBlock.getInsts();
        ArrayList<MCInst> mcInsts = new ArrayList<>();
        CurBlock = new MCBlock(CurFunction, mcInsts,basicBlock.getName());
        if(isEntry) CurBlock.addInst(new MCBinaryInst(MCInst.Tag.addi, PhysicalReg.sp, PhysicalReg.sp, -CurSize));
        for(Instruction instruction : instructions){
            genInst(instruction);
        }
    }
    private void genFunction(Function function) throws IOException {
        String name = function.getName();
        name = name.replace("@", "");

        ArrayList<MCBlock> mcBlocks = new ArrayList<>();
        ArrayList<BasicBlock> basicBlocks = function.getBbs();
        //  全部清零
        CurSize = calSize(function);
        CurSpTop = 0;
        spMap.clear();

        for(int i = 0; i < basicBlocks.size(); i++){
            BasicBlock bb = basicBlocks.get(i);
            genBasicBlock(bb, i == 0);
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
