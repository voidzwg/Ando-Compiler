package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;

public class MCBr extends MCInst{
    private Reg rs1;
    private Reg rs2;
    private int type;
    private String label;

    //  bnez/beqz
    public MCBr(Tag tag, Reg rs1, String label){
        this.tag = tag;
        this.rs1 = rs1;
        this.label = label;
        this.type = 1;
    }

    //  beq/bne
    public MCBr(Tag tag, Reg rs1, Reg rs2, String label){
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.label = label;
        this.type = 2;
    }

    @Override
    public String toString(){
        if(type == 1){
            return tag + " " + rs1 + ", " + label;
        }
        else {
            return tag + " " + rs1 + ", " + rs2 + ", " + label;
        }
    }

}
