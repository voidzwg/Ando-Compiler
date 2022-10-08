package Backend.Reg;

public class VirtualReg extends Reg{
    private static int id = 0;

    private String name;

    public VirtualReg(){
        this.name = "$" + id++;
    }
}
