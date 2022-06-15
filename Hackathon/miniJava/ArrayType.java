
public class ArrayType extends TypeDenoter {

	    public ArrayType(TypeDenoter eltType, SourcePosition posn){
	        super(TypeKind.ARRAY, posn);
	        this.eltType = eltType;
	    }

	    public <A,R> R visit(Visitor<A,R> v, A o) {
	        return v.visitArrayType(this, o);
	    }

	    public TypeDenoter eltType;
	}
