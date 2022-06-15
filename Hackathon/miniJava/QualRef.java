
public class QualRef extends Reference {

	public QualRef(Reference ref, Identifier id, SourcePosition posn){
		super(posn);
		this.ref = ref;
		this.id  = id;
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitQRef(this, o);
	}

	public Reference ref;
	public Identifier id;
	@Override
	public TypeDenoter getType() {
		return id.decl.type;
	}

	@Override
	public Declaration getDecl() {
		return id.decl;
	}

	@Override
	public Identifier getID() {
		return id;
	}
}
