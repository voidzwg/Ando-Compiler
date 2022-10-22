package Backend.MachineValue;

public class MCData {
    //  type: 0表示字符串，1表示数字，2表示数组
    private int type;
    private int num;
    private String string;
    private String name;

    public MCData(String name, String string){
        this.name = name;
        this.string = string;
        this.type = 0;
    }

    public MCData(String name, int num){
        this.name = name;
        this.num = num;
        this.type = 1;
    }

    public String getName(){
        return name;
    }

    public int getType() {
        return type;
    }

    public String getString() {
        return string;
    }

    public int getNum() {
        return num;
    }
}
