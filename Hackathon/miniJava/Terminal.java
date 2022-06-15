
abstract public class Terminal extends AST {

  public Terminal (Token t) {
	super(t.position);
    spelling = t.spelling;
    kind = t.kind;
  }

  public String spelling;
  public int kind;
}
