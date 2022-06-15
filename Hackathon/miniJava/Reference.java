
public abstract class Reference extends AST
{
	public Reference(SourcePosition posn){
		super(posn);
	}

	public Declaration decl;
	public abstract TypeDenoter getType();
	public abstract Declaration getDecl();
	public abstract Identifier getID();

}
