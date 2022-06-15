
public class BaseType extends TypeDenoter
{
    public BaseType(TypeKind t, SourcePosition posn){
        super(t, posn);
    }

    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitBaseType(this, o);
    }
}
