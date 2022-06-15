
public class ThisRef extends BaseRef {

	public ThisRef(SourcePosition posn) {
		super(posn);
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitThisRef(this, o);
	}

	@Override
	public TypeDenoter getType() {
		return null;
	}

	@Override
	public Declaration getDecl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Identifier getID() {
		// TODO Auto-generated method stub
		return null;
	}

}
