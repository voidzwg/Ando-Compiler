package Backend.Reg;

public class PhysicalReg extends Reg{
    private final int num;

    public static PhysicalReg sp = new PhysicalReg("sp");
    public static PhysicalReg x0 = new PhysicalReg("x0");
    public static PhysicalReg a0 = new PhysicalReg("a0");

    public PhysicalReg(String name){
        super(name);
        RegNameMap regNameMap = RegNameMap.getInstance();
        this.num = regNameMap.getRegNum(name);
    }
}
