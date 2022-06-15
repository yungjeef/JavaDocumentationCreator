
public class VarDecl extends LocalDecl {

	public Identifier varId;
	public ClassDecl classDeclaration;

	public VarDecl(TypeDenoter t, String name, Identifier id, SourcePosition posn) {
		super(name, t, posn);
		this.varId = id;
	}

	public <A,R> R visit(Visitor<A,R> v, A o) {
		return v.visitVarDecl(this, o);
	}

}
