
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;

public class IdTable {

	private HashMap<String, Declaration> currentIdTable;
	private HashMap<String, ClassDecl> exclusiveHashMap;
	public ArrayList<HashMap<String, Declaration>> allIdTable;
	public Stack<HashMap<String, Declaration>> scopedIdTable;

	public IdTable() {
		currentIdTable = new HashMap<String, Declaration>();
		exclusiveHashMap = new HashMap<String, ClassDecl>();
		allIdTable = new ArrayList<HashMap<String, Declaration>>();
		allIdTable.add(currentIdTable);
		scopedIdTable = new Stack<HashMap<String, Declaration>>();
		scopedIdTable.push(currentIdTable);
	}

	public IdTable(HashMap<String, Declaration> idTable) {
		currentIdTable = idTable;
		exclusiveHashMap = new HashMap<String, ClassDecl>();
		allIdTable = new ArrayList<HashMap<String, Declaration>>();
		allIdTable.add(currentIdTable);
		scopedIdTable = new Stack<HashMap<String, Declaration>>();
		scopedIdTable.push(currentIdTable);
	}

	//get the top most index
	public int getCurrentLevel() {
		int size = allIdTable.size() - 1;
		return size;
	}

	public HashMap<String, Declaration> getCurrentIdTable() {
		return this.currentIdTable;
	}


	//Scopes--------------------------------------------------

	public void openScope() {
		currentIdTable = new HashMap<String, Declaration>();
		allIdTable.add(currentIdTable);
		scopedIdTable.push(currentIdTable);
	}

	public void openScope(HashMap<String, Declaration> newIdTable) {
		currentIdTable = newIdTable;
		allIdTable.add(currentIdTable);
		scopedIdTable.push(currentIdTable);
	}

	public void closeScope() {
		allIdTable.remove(this.getCurrentLevel());
		scopedIdTable.pop();
		currentIdTable = scopedIdTable.peek();
	}


	//Declaration-----------------------------------------------

	public int enterDecl(Declaration declaration) {
		String idName = declaration.name;
		currentIdTable = getMapAtLevel(getCurrentLevel());

		//indicates there is a duplicate declaration in current scope
		if(currentIdTable.get(idName) != null) {
			return 0;
		}

		for(int i= getCurrentLevel() - 1; i >= 3; i--) {
			if(getMapAtLevel(i).get(idName) != null) {
				return 0;
			}
		}

		currentIdTable.put(idName, declaration);
		return 1;
	}

	public void enterExclusiveMap(Identifier id, ClassDecl classDecl) {
		String idSpelling = id.spelling;
		//System.out.println(classDecl.name);
		exclusiveHashMap.put(idSpelling, classDecl);
	}

	public void enterExclusiveMap(String idSpelling, ClassDecl classDecl) {
		//System.out.println(classDecl.name);
		exclusiveHashMap.put(idSpelling, classDecl);
	}

	public ClassDecl returnExclusiveMap(String idSpelling) {
		if(exclusiveHashMap.get(idSpelling) == null) {
			return null;
		}

		return exclusiveHashMap.get(idSpelling);
	}

	public void clearExclusiveMap() {
		exclusiveHashMap.clear();
	}

	public void printExclusiveMap() {
		for(int i=0; i < exclusiveHashMap.size(); i++) {
			System.out.println("Classes: " + exclusiveHashMap.get(i));
		}
	}

	public int set(int level, HashMap<String, Declaration> levelTable) {
		if(level < 0 || level > getCurrentLevel()) {
			printTable();
			System.out.println("Level is out of bounds");
			System.exit(4);
		} else {
			if(level == getCurrentLevel()) {
				//if it is at top level table, then the current table will be the one to be set upon
				currentIdTable = levelTable;
				allIdTable.set(level, currentIdTable);
			}
			allIdTable.set(level, levelTable);
		}

		return 1;
	}

	public int setEmptyTop(HashMap<String, Declaration> levelTable) {
		if(getMapAtLevel(getCurrentLevel()).size() != 0) {
			System.out.println("Top is not empty");
		}
		return set(getCurrentLevel(), levelTable);
	}

	//finds particular id to see if it exists at all
	public Declaration retrieve(String id) {
		Declaration retDecl = null;
		for(int i = getCurrentLevel(); i >= 0; i--) {
			retDecl = allIdTable.get(i).get(id);
			if(retDecl != null) {
				return retDecl;
			}
		}
		return null;
	}

	public Declaration retrieveClass(String id) {
		Declaration retDecl = null;
		for(int i = getCurrentLevel(); i >= 0; i--) {
			retDecl = allIdTable.get(i).get(id);
			if(retDecl != null) {
				if(retDecl.type instanceof BaseType) {
					continue;
				} else {
					return retDecl;
				}
			}
		}

		return null;
	}

	//gets the hashmap at the particular level
	public HashMap<String, Declaration> getMapAtLevel(int level) {
		return allIdTable.get(level);
	}

	public boolean idExistsAtCurrent(String id) {
		for(String currentId : currentIdTable.keySet()) {
			if(id.equals(currentId)) {
				return true;
			}
		}
		return false;
	}

	public HashMap<String, Declaration> getTopTable() {
		return allIdTable.get(0);
	}

	//very useful for debugging
	public void printTable() {
		for(int i=0; i < allIdTable.size(); i++) {
			System.out.println("Layer " + i + ": " + allIdTable.get(i));
		}
	}

}
