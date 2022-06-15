
public class RefExpr extends Expression
{
    public RefExpr(Reference r, SourcePosition posn){
        super(posn);
        ref = r;
    }

    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitRefExpr(this, o);
    }

    public Reference ref;

    public TypeDenoter getType() {
        return ref.getType();
    }
}
