package Backend.MachineValue.MachineInst;

import Backend.Reg.Reg;
import Backend.Reg.VirtualReg;

import java.util.ArrayList;

public class MCBinaryInst extends MCInst{
    private int imm;
    private int type;

    public MCBinaryInst(Tag tag, Reg rd, Reg rs1, Reg rs2){
        this.tag = tag;
        this.rd = rd;
        this.rs1 = rs1;
        this.rs2 = rs2;
        this.type = 1;
        useReg.add(rs1);
        useReg.add(rs2);
        defReg.add(rd);
    }

    public MCBinaryInst(Tag tag, Reg rd, Reg rs1, int imm){
        this.tag = tag;
        this.rd = rd;
        this.rs1 = rs1;
        this.imm = imm;
        this.type = 2;
        useReg.add(rs1);
        defReg.add(rd);
    }

    public MCBinaryInst(Tag tag, Reg rd, Reg rs){
        this.tag = tag;
        this.rd = rd;
        this.rs1 = rs;
        this.type = 3;
        useReg.add(rs);
        defReg.add(rd);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder(tag.name());
        stringBuilder.append(" ").append(rd);
        stringBuilder.append(", ").append(rs1);
        if(type == 1){
            stringBuilder.append(", ").append(rs2);
        }
        else if(type == 2) stringBuilder.append(", ").append(imm);
        return stringBuilder.toString();
    }

    public void setImm(int imm){
        this.imm = imm;
    }

    public int getImm(){
        return imm;
    }
}
