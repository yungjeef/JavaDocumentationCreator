
public class CallExpr extends Expression
{
    public CallExpr(Reference f, ExprList el, SourcePosition posn){
        super(posn);
        functionRef = f;
        argList = el;
    }

    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitCallExpr(this, o);
    }

    public Reference functionRef;
    public ExprList argList;
}
