
public class NewObjectExpr extends NewExpr
{
    public NewObjectExpr(ClassType ct, SourcePosition posn){
        super(posn);
        classtype = ct;
    }

    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitNewObjectExpr(this, o);
    }

    public ClassType classtype;
}
