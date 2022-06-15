
public class IdRef extends BaseRef {

	public IdRef(Identifier id, SourcePosition posn){
		super(posn);
		this.id = id;
	}

	public <A,R> R visit(Visitor<A,R> v, A o) {
		return v.visitIdRef(this, o);
	}

	public Identifier id;

	@Override
	public TypeDenoter getType() {
		return id.type;
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
