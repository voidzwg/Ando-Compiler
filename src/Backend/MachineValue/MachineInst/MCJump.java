package Backend.MachineValue.MachineInst;

public class MCJump extends MCInst{
    private final String label;
    public MCJump(String label){
        this.label = label;
    }

    @Override
    public String toString(){
        return "j " + label;
    }
}
