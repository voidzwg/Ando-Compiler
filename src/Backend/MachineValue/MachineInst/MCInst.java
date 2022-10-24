package Backend.MachineValue.MachineInst;

import Backend.Reg.MCReg;
import Backend.Reg.Reg;

import java.util.ArrayList;

public abstract class MCInst {

    public enum Tag{
        ret,
        seq,
        sne,
        sle,
        slt,
        slti,
        sge,
        sgt,
        add,
        addi,
        sub,
        xor,
        xori,
        or,
        ori,
        and,
        andi,
        mul,
        div,
        rem,
        li,
        la,
        mv,
        bnez,
        beqz,
        bne,
        beq,
        nop,
        syscall
    }

    protected Reg rd;
    protected Reg rs1;
    protected Reg rs2;

    public Tag tag;
    public ArrayList<Reg> useReg = new ArrayList<>();
    public ArrayList<Reg> defReg = new ArrayList<>();

    public ArrayList<Reg> getUseReg(){
        return useReg;
    }

    public ArrayList<Reg> getDefReg(){
        return defReg;
    }

    public void replaceReg(Reg oldReg, MCReg allocReg){
        if(rd == oldReg){
            rd = allocReg;
        }
        else if(rs1 == oldReg){
            rs1 = allocReg;
        }
        else if(rs2 == oldReg){
            rs2 = allocReg;
        }
    }
}
