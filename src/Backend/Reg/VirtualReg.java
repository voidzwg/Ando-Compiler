package Backend.Reg;

public class VirtualReg extends Reg{
    private static int id = 0;
    private int vid;

    public VirtualReg(){
        super(String.valueOf(id++));
        this.vid = id;
    }

    public int getId(){
        return vid;
    }
}
