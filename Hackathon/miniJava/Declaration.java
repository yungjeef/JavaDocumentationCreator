
public abstract class Declaration extends AST {

	public Declaration(String name, TypeDenoter type, SourcePosition posn) {
		super(posn);
		this.name = name;
		this.type = type;
	}

	public String name;
	public TypeDenoter type;
}
