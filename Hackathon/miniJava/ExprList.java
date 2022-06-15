
import java.util.*;

public class ExprList implements Iterable<Expression>
{
    public ExprList() {
        elist = new ArrayList<Expression>();
    }

    public void add(Expression e){
        elist.add(e);
    }

    public Expression get(int i){
        return elist.get(i);
    }

    public int size() {
        return elist.size();
    }

    public Iterator<Expression> iterator() {
    	return elist.iterator();
    }

    private List<Expression> elist;
}
