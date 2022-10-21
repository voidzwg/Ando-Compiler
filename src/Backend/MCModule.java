package Backend;

import Backend.MachineValue.MCBlock;
import Backend.MachineValue.MCFunction;
import Backend.MachineValue.MachineInst.*;
import Backend.Reg.MCReg;
import Backend.Reg.Reg;
import Backend.Reg.VirtualReg;
import IR.IRModule;
import IR.Value.*;
import IR.Value.Instructions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MCModule {

    //  虚拟寄存器$0表示x0，$1表示a0
    private final ArrayList<MCFunction> mcFunctions = new ArrayList<>();
    private MCFunction CurFunction;
    private MCBlock CurBlock;
    private Function CurIRFunction;
    private int CurSize;
    private String CurFuncName;

    //  regMap用于存储Value到VirReg的映射
    private final HashMap<String, Reg> regMap = new HashMap<>();
    //  spMap存储Reg到sp中的位置
    private final HashMap<Reg, Integer> spMap = new HashMap<>();
    //  saveRegs存储所有需要在call的时候保存的Reg
    private final HashSet<Reg> saveRegs = new HashSet<>();

    //  用来记录函数是否为叶子函数
    private final HashMap<String, Boolean> isLeafMap = new HashMap<>();
    //  查找mcBlock
    private final HashMap<String, MCBlock> mcBlockMap = new HashMap<>();
    private int calAns(OP op, int l, int r){
        if(op == OP.Add) return l + r;
        else if(op == OP.Sub) return l - r;
        else if(op == OP.Mul) return l * r;
        else if(op == OP.Div) return l / r;
        else if(op == OP.Eq){
            if(l == r) return 1;
            else return 0;
        }
        else if(op == OP.Ne){
            if(l == r) return 0;
            else return 1;
        }
        else if(op == OP.Le){
            if(l <= r) return 1;
            else return 0;
        }
        else if(op == OP.Lt){
            if(l < r) return 1;
            else return 0;
        }
        else if(op == OP.Ge){
            if(l >= r) return 1;
            else return 0;
        }
        else if(op == OP.Gt){
            if(l > r) return 1;
            else return 0;
        }
        return 0;
    }

    private int CurSpTop = 0;

    private int calSize(Function function){
        int size = 0;
        boolean isLeaf = true;
        //  传入的fParams
        size += function.getArgs().size() * 4;

        ArrayList<BasicBlock> bbs = function.getBbs();
        for(BasicBlock bb : bbs){
            ArrayList<Instruction> insts = bb.getInsts();
            for(Instruction inst : insts){
                if(inst.hasName()){
                    size += 4;
                }
                if(inst instanceof CallInst){
                    isLeaf = false;
                    CallInst callInst = (CallInst) inst;
                    int argNum = callInst.getValues().size();
                    //  arg里有常数也有参数，干脆都算空间
                    size += argNum * 4;
                }
            }
        }
        isLeafMap.put(function.getName().replace("@", ""), isLeaf);
        //  预留给ra的
        if(!isLeaf) size += 4;
        return size;
    }

    private Reg buildVirReg(){
        Reg reg = new VirtualReg();
        saveRegs.add(reg);
        return reg;
    }

    private Reg val2Reg(Value value){
        String ident = value.getName();
        if(regMap.containsKey(ident)){
            return regMap.get(ident);
        }
        //  value为常数时，我们不应该建立映射
        //  因为该寄存器内的值会不断改变
        if(value instanceof ConstInteger){
            int num = Integer.parseInt(ident);
            Reg reg;
            if(num == 0) return MCReg.zero;
            else reg = buildVirReg();
            CurBlock.addInst(new MCLI(reg, num));
            return reg;
        }
        else{
            Reg reg = buildVirReg();
            regMap.put(ident, reg);
            return reg;
        }
    }

    //  这个函数命名确实可能有点抽象，解释一下
    //  全写为 raw BasicBlock Name To Machine Code BasicBlock Name
    private String rawBbName2MCBbName(String rawBbName){
        return CurFuncName + "_" + rawBbName.replace("%", "");
    }

    private MCInst.Tag OP2Tag(OP op){
        if(op == OP.Sub) return MCInst.Tag.sub;
        else if(op == OP.Add) return MCInst.Tag.add;
        else if(op == OP.Mul) return MCInst.Tag.mul;
        else if(op == OP.Div) return MCInst.Tag.div;
        else if(op == OP.Eq) return MCInst.Tag.seq;
        else if(op == OP.Ne) return MCInst.Tag.sne;
        else if(op == OP.Le) return MCInst.Tag.sle;
        else if(op == OP.Lt) return MCInst.Tag.slt;
        else if(op == OP.Ge) return MCInst.Tag.sge;
        else if(op == OP.Gt) return MCInst.Tag.sgt;
        return null;
    }

    private void epilogue(){
        //  1. 恢复ra
        if(!isLeafMap.get(CurFuncName)) {
            CurBlock.addInst(new MCLW(MCReg.ra, MCReg.sp, CurSize - 4));
        }

        //  2. 复原栈顶
        CurBlock.addInst(new MCBinaryInst(MCInst.Tag.addi, MCReg.sp, MCReg.sp, CurSize));

        //  3. 生成jr ra或syscall
        if(!CurFuncName.equals("main")) {
            CurBlock.addInst(new MCJump("$ra", 1));
            CurBlock.addInst(new MCOther(MCInst.Tag.nop));
        }
        else {
            CurBlock.addInst(new MCLI(new MCReg("v0"), 10));
            CurBlock.addInst(new MCOther(MCInst.Tag.syscall));
        }

    }
    private void genInst(Instruction instruction){
        if(instruction instanceof RetInst){
            RetInst retInst = (RetInst) instruction;
            Value retVal = retInst.getValue();
            if(retVal instanceof ConstInteger) {
                ConstInteger intConst = (ConstInteger) retInst.getValue();
                int imm = intConst.getVal();

                CurBlock.addInst(new MCLI(MCReg.a0, imm));
            }
            else if(!retVal.getName().equals("void")){
                Reg reg = val2Reg(retInst.getValue());
                CurBlock.addInst(new MCMV(MCReg.a0, reg));
            }
            epilogue();
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
            //  add, sub, mul, div
            //  全是constInteger，我他妈直接运算
            if(isImm == 2){
                int leftVal = ((ConstInteger) left).getVal();
                int rightVal = ((ConstInteger) right).getVal();

                Reg reg = buildVirReg();
                CurBlock.addInst(new MCLI(reg, calAns(op, leftVal, rightVal)));
            }

            else if(isImm == 1 && (op == OP.Sub || op == OP.Add)){
                ConstInteger constInteger = (ConstInteger) tmpRight;
                int val = constInteger.getVal();
                Reg rs1 = val2Reg(tmpLeft);
                Reg rd = val2Reg(binaryInst);
                if(op == OP.Sub) val = -val;
                CurBlock.addInst(new MCBinaryInst(MCInst.Tag.addi, rd, rs1, val));
            }

            else {
                Reg rs1 = val2Reg(left);
                Reg rs2 = val2Reg(right);
                Reg rd = val2Reg(binaryInst);
                MCInst.Tag tag = OP2Tag(op);
                CurBlock.addInst(new MCBinaryInst(tag, rd, rs1, rs2));
            }

        }
        else if(instruction instanceof CmpInst){
            CmpInst cmpInst = (CmpInst) instruction;
            OP op = cmpInst.getOp();
            Value left = cmpInst.getLeftVal();
            Value right = cmpInst.getRightVal();
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
            if(isImm == 2){
                int leftVal = ((ConstInteger) left).getVal();
                int rightVal = ((ConstInteger) right).getVal();
                int ans;
                ans = calAns(op, leftVal, rightVal);

                Reg reg = buildVirReg();

                CurBlock.addInst(new MCLI(reg, ans));
                regMap.put(cmpInst.getName(), reg);
            }
            else if(isImm == 1){
                int imm = ((ConstInteger) tmpRight).getVal();
                Reg rs1 = val2Reg(tmpLeft);
                Reg rd = val2Reg(cmpInst);
                if(op == OP.Lt) CurBlock.addInst(new MCBinaryInst(MCInst.Tag.slti, rd, rs1, imm));
                else CurBlock.addInst(new MCBinaryInst(OP2Tag(op), rd, rs1, imm));
            }
            else{
                Reg rs1 = val2Reg(left);
                Reg rs2 = val2Reg(right);
                Reg rd = val2Reg(cmpInst);
                CurBlock.addInst(new MCBinaryInst(OP2Tag(op), rd, rs1, rs2));
            }
        }
        else if(instruction instanceof StoreInst){
            StoreInst storeInst = (StoreInst) instruction;
            Reg rs = val2Reg(storeInst.getValue());
            Reg rd = val2Reg(storeInst.getPointer());
            CurBlock.addInst(new MCMV(rd, rs));
        }
        else if(instruction instanceof LoadInst){
            LoadInst loadInst = (LoadInst) instruction;
            Reg rd = val2Reg(loadInst);
            Reg rs = val2Reg(loadInst.getPointer());
            CurBlock.addInst(new MCMV(rd, rs));
        }
        else if(instruction instanceof BrInst){
            BrInst brInst = (BrInst) instruction;
            if(brInst.isJump()){
                BasicBlock jumpBB = brInst.getLabelJump();
                CurBlock.addInst(new MCJump(rawBbName2MCBbName(jumpBB.getName()), 0));
                CurBlock.addInst(new MCOther(MCInst.Tag.nop));
                CurBlock.setTrueBlock(mcBlockMap.get(rawBbName2MCBbName(jumpBB.getName())));
            }
            else{
                String leftLabel = brInst.getLabelLeft().getName();
                String rightLabel = brInst.getLabelRight().getName();
                String mcLeftName = rawBbName2MCBbName(leftLabel);
                String mcRightName = rawBbName2MCBbName(rightLabel);
                Reg reg = val2Reg(brInst.getJudVal());
                CurBlock.addInst(new MCBr(MCInst.Tag.bnez, reg, mcLeftName));
                CurBlock.setTrueBlock(mcBlockMap.get(mcLeftName));
                CurBlock.addInst(new MCJump(mcRightName, 0));
                CurBlock.addInst(new MCOther(MCInst.Tag.nop));
                CurBlock.setFalseBlock(mcBlockMap.get(mcRightName));
            }
        }
        else if(instruction instanceof CallInst){
            CallInst callInst = (CallInst) instruction;

            saveAll(callInst);

            String blockName = callInst.getCallFunc().getName().replace("@", "") + "_0";
            CurBlock.addInst(new MCJump(blockName, 2));
            CurBlock.addInst(new MCOther(MCInst.Tag.nop));

            //  保存返回值
            if(callInst.hasName()){
                Reg reg = val2Reg(callInst);
                CurBlock.addInst(new MCMV(reg, MCReg.a0));
                saveRegs.remove(reg);
            }
            loadAll();
            if(callInst.hasName()) {
                saveRegs.add(val2Reg(callInst));
            }
        }
    }

    private void loadAll(){
        for(Reg reg : saveRegs){
            CurBlock.addInst(new MCLW(reg, MCReg.sp, spMap.get(reg)));
        }
    }

    private void saveAll(CallInst callInst){
        saveAllReg();
        ArrayList<Value> rParams = callInst.getValues();
        for(int i = 0; i < rParams.size(); i++){
            Value value = rParams.get(i);
            if(i < 4){
                String regName = "a" + i;
                if(value instanceof ConstInteger){
                    CurBlock.addInst(new MCLI(new MCReg(regName), ((ConstInteger) value).getVal()));
                }
                else{
                    Reg reg = val2Reg(value);
                    CurBlock.addInst(new MCMV(new MCReg(regName), reg));
                }
            }
            else saveRParam(value, i * 4);
        }
    }

    //  saveRegs在马上要call其他函数的时候被调用
    //  saveRegs保存所有该函数目前要使用的寄存器的值
    private void saveAllReg(){
        for(Reg reg : saveRegs){
            if(spMap.containsKey(reg)){
                int pos = spMap.get(reg);
                CurBlock.addInst(new MCSW(reg, MCReg.sp, pos));
            }
            else {
                CurBlock.addInst(new MCSW(reg, MCReg.sp, CurSpTop));
                spMap.put(reg, CurSpTop);
                CurSpTop -= 4;
            }
        }
    }

    private void saveRParam(Value exRParam, int pos){
        Reg reg;
        //  在栈帧上存储参数需要从低到高存储
        if(exRParam instanceof ConstInteger){
            reg = new VirtualReg();
            CurBlock.addInst(new MCLI(reg, ((ConstInteger) exRParam).getVal()));
            CurBlock.addInst(new MCSW(reg, MCReg.sp, pos));
        }
        else {
            reg = val2Reg(exRParam);
            CurBlock.addInst(new MCSW(reg, MCReg.sp, pos));
        }
    }

    //  prologue处理一些函数的初始化工作
    //  例如接受参数，保存ra，初始化sp
    private void prologue(){
        //  1. 接受参数
        for(int i = 0; i < CurIRFunction.getArgs().size(); i++){
            String argName = "a" + i;
            Value arg = CurIRFunction.getArgs().get(i);
            Reg reg = val2Reg(arg);
            if(i < 4) CurBlock.addInst(new MCMV(reg, new MCReg(argName)));
            else {
                CurBlock.addInst(new MCLW(reg, MCReg.sp, i * 4));
            }
        }

        //  2. 设置栈顶
        CurSize = calSize(CurIRFunction);
        CurBlock.addInst(new MCBinaryInst(MCInst.Tag.addi, MCReg.sp, MCReg.sp, -CurSize));
        CurSpTop = CurSize - 4;

        //  3. 保存ra
        //  ra和局部变量从高地址向低地址放，超过八个的函数参数从低地址向高地址放

        if(!isLeafMap.get(CurFuncName)){
            CurBlock.addInst(new MCSW(MCReg.ra, MCReg.sp, CurSpTop));
            spMap.put(MCReg.ra, CurSpTop);
            CurSpTop -= 4;
        }
    }

    //  genFunction已经提前为genBasicBlock初始化好了CurBlock，
    //  genBasicBlock直接添加指令就可以
    private void genBasicBlock(BasicBlock basicBlock){
        ArrayList<Instruction> instructions = basicBlock.getInsts();

        if(CurBlock.isEntry()){
            prologue();
        }
        for(Instruction instruction : instructions){
            genInst(instruction);
        }
    }
    private void genFunction(Function function){
        CurFuncName = function.getName().replace("@", "");
        CurIRFunction = function;
        ArrayList<MCBlock> mcBlocks = new ArrayList<>();
        ArrayList<BasicBlock> basicBlocks = function.getBbs();
        //  初始化，该清零的清零，该设置的设置
        spMap.clear();
        saveRegs.clear();
        mcBlockMap.clear();

        for (int i = 0; i < basicBlocks.size(); i++) {
            boolean isEntry = (i == 0);
            BasicBlock bb = basicBlocks.get(i);
            bb.setName("%" + i);
            String mcBlockName = rawBbName2MCBbName(bb.getName());
            MCBlock mcBlock = new MCBlock(CurFunction, new ArrayList<>(), mcBlockName, isEntry);
            mcBlockMap.put(mcBlockName, mcBlock);
        }

        for (BasicBlock bb : basicBlocks) {
            String mcBlockName = rawBbName2MCBbName(bb.getName());
            CurBlock = mcBlockMap.get(mcBlockName);
            genBasicBlock(bb);
            mcBlocks.add(CurBlock);
        }

        CurFunction = new MCFunction(CurFuncName, mcBlocks);
        mcFunctions.add(CurFunction);
    }

    public void genMips(IRModule irModule){
        ArrayList<Function> functions = irModule.getFunctions();
        for(Function function : functions){
            genFunction(function);
        }
    }

    public ArrayList<MCFunction> getMcFunctions() {
        return mcFunctions;
    }
}
