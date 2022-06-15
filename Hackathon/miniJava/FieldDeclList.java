
import java.util.*;

public class FieldDeclList implements Iterable<FieldDecl>
{
	public FieldDeclList() {
		fieldDeclList = new ArrayList<FieldDecl>();
	}

	public void add(FieldDecl cd){
		fieldDeclList.add(cd);
	}

	public FieldDecl get(int i){
		return fieldDeclList.get(i);
	}

	public int size() {
		return fieldDeclList.size();
	}

	public Iterator<FieldDecl> iterator() {
		return fieldDeclList.iterator();
	}

	private List<FieldDecl> fieldDeclList;
}
