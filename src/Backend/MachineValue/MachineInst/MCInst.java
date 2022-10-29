package Backend.MachineValue.MachineInst;

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
        addu,
        addiu,
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

    public void replaceReg(Reg oldReg, Reg allocReg){
        if(rd == oldReg){
            rd = allocReg;
            defReg = new ArrayList<>();
            defReg.add(rd);
        }
        else if(rs1 == oldReg){
            rs1 = allocReg;
            useReg.remove(oldReg);
            useReg.add(rs1);
        }
        else if(rs2 == oldReg){
            rs2 = allocReg;
            useReg.remove(oldReg);
            useReg.add(rs1);
        }
    }

    public Reg getRd(){
        return rd;
    }

    public Reg getRs1(){
        return rs1;
    }

    public Reg getRs2(){
        return rs2;
    }

    public Tag getTag(){
        return tag;
    }

    public boolean isJr(){
        return this instanceof MCJump && ((MCJump) this).getType() == 1;
    }

    public boolean isSysCall(){
        return this.tag == Tag.syscall;
    }
}
