package Backend.Reg;

public class MCReg extends Reg{
    protected boolean isAllocated = false;

    public static MCReg sp = new MCReg("sp", false);
    public static MCReg ra = new MCReg("ra", false);
    public static MCReg zero = new MCReg("zero", false);
    public static MCReg a0 = new MCReg("a0", false);
    public static MCReg a1 = new MCReg("a1", false);
    public static MCReg a2 = new MCReg("a2", false);
    public static MCReg a3 = new MCReg("a3", false);
    public static MCReg v0 = new MCReg("v0", false);
    private RegNameMap regNameMap = RegNameMap.getInstance();

    public MCReg(String name, boolean isAllocated){
        super(name);
        this.isAllocated = isAllocated;
        this.id = regNameMap.getRegNum(name);
    }

    public MCReg(int pid, boolean isAllocated){
        super(pid);
        this.isAllocated = isAllocated;
        this.name = regNameMap.getRegName(pid);
    }

    public int getId(){
        return id;
    }

    public int getPreColor(){
        return id;
    }
}
