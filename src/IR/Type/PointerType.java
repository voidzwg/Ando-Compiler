package IR.Type;

public class PointerType extends Type{
    Type ContentType;

    public PointerType(Type ContentType){
        this.ContentType = ContentType;
    }

}
