
public class WhileStmt extends Statement
{
    public WhileStmt(Expression e, Statement s, SourcePosition posn){
        super(posn);
        cond = e;
        body = s;
    }

    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitWhileStmt(this, o);
    }

    public Expression cond;
    public Statement body;
}
