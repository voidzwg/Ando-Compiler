package Backend.MachineValue.MachineInst;

import Backend.Reg.VirtualReg;

public class MCBinaryInst extends MCInst{
    private VirtualReg rd;
    private VirtualReg rs1;
    private VirtualReg rs2;
    private int imm;
    private int type;

    public MCBinaryInst(Tag tag, VirtualReg rd, VirtualReg rs1, VirtualReg rs2){
        this.tag = tag;
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.type = 1;
    }

    public MCBinaryInst(Tag tag, VirtualReg rd, VirtualReg rs1, int imm){
        this.tag = tag;
        this.rd = rd;
        this.rs1 = rs1;
        this.imm = imm;
        this.type = 2;
    }

    public MCBinaryInst(Tag tag, VirtualReg rd, VirtualReg rs){
        this.tag = tag;
        this.rd = rd;
        this.rs1 = rs;
        this.type = 3;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder(tag.name());
        stringBuilder.append(" ").append(rd);
        stringBuilder.append(" ").append(rs1);
        if(type == 1){
            stringBuilder.append(" ").append(rs2);
        }
        else if(type == 2) stringBuilder.append(" ").append(imm);
        return stringBuilder.toString();
    }

}
