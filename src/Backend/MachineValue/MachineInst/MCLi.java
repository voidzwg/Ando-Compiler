package Backend.MachineValue.MachineInst;

public class MCLi extends MCInst{
    private int imm;

    public MCLi(int imm){
        this.imm = imm;
    }

    public int getImm() {
        return imm;
    }
}
