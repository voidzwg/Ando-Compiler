package Backend.MachineValue.MachineInst;

import Backend.Reg.VirtualReg;

public class MCBinaryInst extends MCInst{
    private VirtualReg rd;
    private VirtualReg rs1;
    private VirtualReg rs2;
    private int imm;

    public MCBinaryInst(Tag tag, VirtualReg rd, VirtualReg rs1, VirtualReg rs2){
        this.tag = tag;
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
    }

    public MCBinaryInst(Tag tag, VirtualReg rd, VirtualReg rs1, int imm){
        this.tag = tag;
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = null;
        this.imm = imm;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder(tag.name());
        stringBuilder.append(" ").append(rd);
        stringBuilder.append(" ").append(rs1);
        if(rs2 == null){
            stringBuilder.append(" ").append(imm);
        }
        else stringBuilder.append(" ").append(rs2);
        return stringBuilder.toString();
    }

}
