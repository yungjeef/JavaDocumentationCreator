
abstract public class MemberDecl extends Declaration {

    public MemberDecl(boolean isPrivate, boolean isStatic, TypeDenoter mt, String name, SourcePosition posn) {
        super(name, mt, posn);
        this.isPrivate = isPrivate;
        this.isStatic = isStatic;
        this.isClass = true;
    }

    public MemberDecl(MemberDecl md, SourcePosition posn){
    	super(md.name, md.type, posn);
    	this.isPrivate = md.isPrivate;
    	this.isStatic = md.isStatic;
    	this.isClass = true;
    }

    public boolean isPrivate;
    public boolean isStatic;

    //added for pa3
    public ClassDecl classDecl;
    public boolean isClass;
}
