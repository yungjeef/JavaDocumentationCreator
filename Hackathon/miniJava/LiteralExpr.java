
public class LiteralExpr extends Expression
{
    public LiteralExpr(Terminal t, SourcePosition posn){
        super(t.posn);
        lit = t;
    }

    public <A,R> R visit(Visitor<A,R> v, A o){
        return v.visitLiteralExpr(this, o);
    }

    public Terminal lit;
    public boolean isNullPresent = false;
}
