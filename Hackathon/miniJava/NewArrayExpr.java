
public class NewArrayExpr extends NewExpr
{
    public NewArrayExpr(TypeDenoter et, Expression e, SourcePosition posn){
        super(posn);
        eltType = et;
        sizeExpr = e;
    }

    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitNewArrayExpr(this, o);
    }

    public TypeDenoter eltType;
    public Expression sizeExpr;
}
