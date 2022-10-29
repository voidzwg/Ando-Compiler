package Backend.Reg;

public class Reg {
    public String name;
    public int id;
    public Reg(String name){
        this.name = name;
    }
    public Reg(int id){
        this.id = id;
    }
    //  isAllocated表示这个寄存器是否用于分配，因此不是用于分配的肯定是abi约定了的寄存器
    public boolean isPrecolored() {
        return this instanceof MCReg && !((MCReg) this).isAllocated;
    }

    @Override
    public String toString(){
        return "$" + name;
    }

    public void setName(String name){
        this.name = name;
    }
}
