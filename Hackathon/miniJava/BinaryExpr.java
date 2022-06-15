
public class BinaryExpr extends Expression
{
    public BinaryExpr(Operator o, Expression e1, Expression e2, SourcePosition posn){
        super(posn);
        operator = o;
        left = e1;
        right = e2;
    }

    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitBinaryExpr(this, o);
    }

    public Operator operator;
    public Expression left;
    public Expression right;
}
