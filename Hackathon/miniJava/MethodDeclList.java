
import java.util.*;

public class MethodDeclList implements Iterable<MethodDecl>
{
	public MethodDeclList() {
		methodDeclList = new ArrayList<MethodDecl>();
	}

	public void add(MethodDecl cd){
		methodDeclList.add(cd);
	}

	public MethodDecl get(int i){
		return methodDeclList.get(i);
	}

	public int size() {
		return methodDeclList.size();
	}

	public Iterator<MethodDecl> iterator() {
		return methodDeclList.iterator();
	}

	private List<MethodDecl> methodDeclList;
}
