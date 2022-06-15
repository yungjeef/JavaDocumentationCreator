
public class Operator extends Terminal {

  public Operator (Token t) {
    super (t);
  }

  public <A,R> R visit(Visitor<A,R> v, A o) {
      return v.visitOperator(this, o);
  }
}
