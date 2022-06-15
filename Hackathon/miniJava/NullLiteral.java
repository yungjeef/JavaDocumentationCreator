
//pa3 added
public class NullLiteral extends Terminal{

	public NullLiteral(Token t) {
		super(t);
	}

	public <A,R> R visit(Visitor<A,R> v, A o) {
	      return v.visitNullLiteral(this, o);
	  }

}
