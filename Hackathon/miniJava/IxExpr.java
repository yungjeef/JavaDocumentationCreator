
public class IxExpr extends Expression {

public IxExpr(Reference r, Expression e, SourcePosition posn){
    super(posn);
    ref = r;
    ixExpr = e;
}

public <A,R> R visit(Visitor<A,R> v, A o) {
    return v.visitIxExpr(this, o);
}

public Reference ref;
public Expression ixExpr;

}
