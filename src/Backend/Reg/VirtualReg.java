package Backend.Reg;

public class VirtualReg extends Reg{
    private static int id = 2;

    private String name;

    public VirtualReg(){
        this.name = "$" + id++;
    }

    public VirtualReg(int id){
        this.name = "$" + id;
    }

    @Override
    public String toString(){
        return name;
    }
}
