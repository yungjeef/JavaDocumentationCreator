
public class Identifier extends Terminal {

	public Declaration decl;
	public TypeDenoter type;
	public boolean isStatic;
	public boolean isLocal; //added for pa3 to determine if the variable is local or not
	public VarDecl varDecl;

  public Identifier (Token t) {
    super (t);
    decl = null;
    type = null;
    isLocal = false;
  }

  public <A,R> R visit(Visitor<A,R> v, A o) {
      return v.visitIdentifier(this, o);
  }

}
