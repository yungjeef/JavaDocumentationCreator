
public class MethodDecl extends MemberDecl {

	public MethodDecl(MemberDecl md, ParameterDeclList pl, StatementList sl, SourcePosition posn, String docsString){
    super(md,posn);
    parameterDeclList = pl;
    statementList = sl;
		this.docsString = docsString;
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
        return v.visitMethodDecl(this, o);
    }

	public ParameterDeclList parameterDeclList;
	public StatementList statementList;
	public TypeDenoter returnType; //pa3 added
	public String docsString;
}
