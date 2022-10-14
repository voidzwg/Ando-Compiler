package Backend.Reg;

public class MCReg extends Reg{
    private final int id;
    public boolean isAllocated = false;

    public static MCReg sp = new MCReg("sp");
    public static MCReg zero = new MCReg("zero");
    public static MCReg a0 = new MCReg("a0");

    public MCReg(String name){
        super(name);
        RegNameMap regNameMap = RegNameMap.getInstance();
        this.id = regNameMap.getRegNum(name);
    }
}
