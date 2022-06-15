import java.util.*;

public class ClassDecl extends Declaration {

  public ClassDecl(String cn, FieldDeclList fdl, MethodDeclList mdl, SourcePosition posn, String docsString) {
	  super(cn, null, posn);
	  name = cn;
	  fieldDeclList = fdl;
	  methodDeclList = mdl;
    this.docsString = docsString;
  }

  public <A,R> R visit(Visitor<A, R> v, A o) {
      return v.visitClassDecl(this, o);
  }

  public HashMap<String, Declaration> getHashMap() {
	  return classDeclMap;
  }

  public HashMap<String, Declaration> initiateHashMap() {
	  return initiateHashMap(1);
  }

  public int level;

  public HashMap<String, Declaration> initiateHashMap(int setlevel) {
	  classDeclMap = new HashMap<String, Declaration>();
	  level = setlevel;

	  for(FieldDecl fd : fieldDeclList) {
		  classDeclMap.put(fd.name, fd);
	  }

	  for(MethodDecl md : methodDeclList) {
		  classDeclMap.put(md.name, md);
	  }

	  return classDeclMap;
  }

  public boolean existsFieldOrMethod(String name, boolean isStatic, boolean isPublic) {
	  for(FieldDecl fd : fieldDeclList) {
		  if(name.equals(fd.name)) {
			  //both have to be the same
			  //static or not static, public or private
			  if(isStatic && !fd.isStatic) {
				  continue;
			  }
			  if(isPublic && fd.isPrivate) {
				  continue;
			  }
			  return true;
		  }
	  }

	  for(MethodDecl md : methodDeclList) {
		  if(name.equals(md.name)) {
			  if(isStatic && !md.isStatic) {
				  continue;
			  }
			  if(isPublic && md.isPrivate) {
				  continue;
			  }
			  return true;
		  }
	  }

	  return false;
  }

  //added another field to get the class name
  public String name;
  public FieldDeclList fieldDeclList;
  public MethodDeclList methodDeclList;
  public int nonStaticField;
  public String docsString;
  //added another hashmap
  protected HashMap<String, Declaration> classDeclMap;
}
