package Backend.MachineValue.MachineInst;

public abstract class MCInst {

    public enum Tag{
        ret,
        seqz,
        snez,
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
    }

    public Tag tag;


}
