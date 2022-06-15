
public abstract class Expression extends AST {

  public Expression(SourcePosition posn) {
    super (posn);
  }

  public TypeDenoter type; //pa3 added to determine which type of expression it is
}
