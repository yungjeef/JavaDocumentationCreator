
abstract public class TypeDenoter extends AST {

    public TypeDenoter(TypeKind type, SourcePosition posn){
        super(posn);
        typeKind = type;
    }

    public TypeKind typeKind;

}
