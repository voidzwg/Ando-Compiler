package Backend.Reg;

public class VirtualReg extends Reg{
    private static int id = 0;

    public VirtualReg(){
        super(String.valueOf(id++));
    }
}
