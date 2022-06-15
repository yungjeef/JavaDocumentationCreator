
public class FieldDecl extends MemberDecl {
	public Identifier fid;
	public String docsString;
	public FieldDecl(boolean isPrivate, boolean isStatic, TypeDenoter t, String name, Identifier id, SourcePosition posn, String docsString){
    super(isPrivate, isStatic, t, name, posn);
    	this.fid = id;
			this.docsString = docsString;
	}

	public FieldDecl(boolean isPrivate, boolean isStatic, TypeDenoter t, String name, Identifier id, SourcePosition posn){
    super(isPrivate, isStatic, t, name, posn);
    	this.fid = id;
	}

	public FieldDecl(boolean isPrivate, boolean isStatic, TypeDenoter t, String name, SourcePosition posn){
	    super(isPrivate, isStatic, t, name, posn);
	}

	public FieldDecl(MemberDecl md, SourcePosition posn) {
		super(md,posn);
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
        return v.visitFieldDecl(this, o);
    }
}
