
public class ParameterDecl extends LocalDecl {

	public Identifier pid;

	public ParameterDecl(TypeDenoter t, String name, Identifier id, SourcePosition posn){
		super(name, t, posn);
		this.pid = id;
	}

	public ParameterDecl(TypeDenoter t, String name, SourcePosition posn){
		super(name, t, posn);
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
        return v.visitParameterDecl(this, o);
    }
}
