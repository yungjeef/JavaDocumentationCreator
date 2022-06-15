
public class VarDeclStmt extends Statement
{
    public VarDeclStmt(VarDecl vd, Expression e, SourcePosition posn){
        super(posn);
        varDecl = vd;
        initExp = e;
    }

    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitVardeclStmt(this, o);
    }

    public VarDecl varDecl;
    public Expression initExp;
}
