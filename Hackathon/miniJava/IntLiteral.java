
public class IntLiteral extends Terminal {

  public IntLiteral(Token t) {
    super(t);
  }

  public <A,R> R visit(Visitor<A,R> v, A o) {
      return v.visitIntLiteral(this, o);
  }
}
