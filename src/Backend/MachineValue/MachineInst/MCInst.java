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
        nop
    }

    public Tag tag;
    public ArrayList<Reg> useReg = new ArrayList<>();
    public ArrayList<Reg> defReg = new ArrayList<>();

    public ArrayList<Reg> getUseReg(){
        return useReg;
    }

    public ArrayList<Reg> getDefReg(){
        return defReg;
    }
}
