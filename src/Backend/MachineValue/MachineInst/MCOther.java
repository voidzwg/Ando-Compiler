package Backend.MachineValue.MachineInst;

public class MCOther extends MCInst{
    public MCOther(Tag tag){
        this.tag = tag;
    }

    @Override
    public String toString(){
        return tag.name();
    }
}
