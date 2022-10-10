package Backend.MachineValue.MachineInst;

public abstract class MCInst {

    public enum Tag{
        ret,
        seq,
        sne,
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
        beq
    }

    public Tag tag;


}
