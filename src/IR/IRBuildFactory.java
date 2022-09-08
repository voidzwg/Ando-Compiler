package IR;

public class IRBuildFactory {
    private IRBuildFactory(){}

    private static IRBuildFactory f = new IRBuildFactory();

    public static IRBuildFactory getInstance(){
        return f;
    }
}
