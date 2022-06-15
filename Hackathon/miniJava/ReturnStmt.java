
public class ReturnStmt extends Statement
{
	public ReturnStmt(Expression e, SourcePosition posn){
		super(posn);
		returnExpr = e;
	}

	public <A,R> R visit(Visitor<A,R> v, A o) {
		return v.visitReturnStmt(this, o);
	}

	public Expression returnExpr;
}
