
public class BooleanLiteral extends Terminal {

  public BooleanLiteral(Token t) {
    super (t);
  }

  public <A,R> R visit(Visitor<A,R> v, A o) {
      return v.visitBooleanLiteral(this, o);
  }
}
