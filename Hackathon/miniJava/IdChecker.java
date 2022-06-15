
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;

public class IdChecker implements Visitor<Integer, Object>{

	private AST ast;
	public ErrorReporter errorReporter;
	public IdTable idTable;

	private ClassDecl currentClass;
	private boolean isStatic;
	public boolean predefinedNames;
	private int numberOfErrors;

	public IdChecker(AST ast, ErrorReporter reporter) {
		this.isStatic = false;
		this.currentClass = null;
		this.ast = ast;
		this.errorReporter = reporter;
		this.idTable = new IdTable();
		this.currentClass = null; //will be set to null until entry to a class

	}

	public AST check() {

		idTable.openScope();
		ast.visit(this, 0);
		idTable.closeScope();

		if(numberOfErrors > 0) {
			System.out.println("Errors exist within the code for id checking");
			System.exit(4);
		} else {
			System.out.println("Code is error free for id checking!");
		}

		return ast;
	}

	//------------------This section is for error logging---------------------

	public void addError(String errorMsg, String name, SourcePosition posn) {
		numberOfErrors++;
		String errorString = "***Error in the class " + currentClass.name + " : " + errorMsg + " involving " + name
				+ " at " + posn.start;
		System.out.println(errorString);
		errorReporter.reportError(errorString);
	}

	//-------------------End of error logging section-------------------------

	@Override
	public Object visitPackage(Package prog, Integer arg) {
		ClassDeclList listOfClasses = prog.classDeclList;

		//------------------------Predefined names---------------------------

		//building class declaration for _PrintStream
		//class _PrintStream { public void println(int n){}; }
		ParameterDeclList psPr = new ParameterDeclList();
		psPr.add(new ParameterDecl(new BaseType(TypeKind.INT, null), "n", null));

		MemberDecl psMemDecl = new FieldDecl(false, false, new BaseType(TypeKind.VOID, null), "println", null);
		StatementList psStList = new StatementList();
		MethodDecl psMdDecl = new MethodDecl(psMemDecl, psPr, psStList, null);
		MethodDeclList psMd = new MethodDeclList();
		psMd.add(psMdDecl);
		ClassDecl printStreamDecl = new ClassDecl("_PrintStream", new FieldDeclList(), psMd, null);
		printStreamDecl.initiateHashMap(idTable.getCurrentLevel());
		idTable.enterDecl(printStreamDecl);

		//building class declaration for System
		//class System { public static _PrintStream out; }
		FieldDeclList sysFd = new FieldDeclList();
		sysFd.add(new FieldDecl(false, true, printStreamDecl.type, "out", null));
		ClassDecl systemDecl = new ClassDecl("System", sysFd, new MethodDeclList(), null);
		systemDecl.initiateHashMap(idTable.getCurrentLevel());
		idTable.enterDecl(systemDecl);
		idTable.enterExclusiveMap("out", printStreamDecl);

		//building class declaration for String
		//class String { }
		ClassDecl stringDecl = new ClassDecl("String", new FieldDeclList(), new MethodDeclList(), null);
		stringDecl.type = new BaseType(TypeKind.UNSUPPORTED, null);
		idTable.enterDecl(stringDecl);

		//--------------------End of predefined names------------------------

		//first iteration to get all the classes
		for(ClassDecl cd : listOfClasses) {
			currentClass = cd;
			if(idTable.enterDecl(cd) == 0) {
				addError("Error: Duplicate class name ", cd.name, cd.posn);
			} else {
				cd.initiateHashMap(idTable.getCurrentLevel());
				idTable.enterDecl(cd);
			}
		}

		for(ClassDecl cd : listOfClasses) {
			FieldDeclList fdl = cd.fieldDeclList;
			for(FieldDecl fd : fdl) {
				if(fd.type instanceof ClassType) {
					ClassDecl fieldClassDecl = visitClassType2((ClassType) fd.type);
					idTable.enterExclusiveMap(fd.fid, fieldClassDecl);
				}
			}

			//add all methods to the list beforehand, to resolve fail336
			MethodDeclList mdl = cd.methodDeclList;
			for(MethodDecl md : mdl) {
				//if method is of a particular class
				if(md.type instanceof ClassType) {
					ClassDecl methodClassDecl = visitClassType2((ClassType) md.type);
					idTable.enterExclusiveMap(md.name, methodClassDecl);
				}
			}
		}

		for(ClassDecl cd : listOfClasses) {
			currentClass = cd;
			idTable.openScope(cd.getHashMap());
			cd.visit(this, 0);
			idTable.closeScope();
			idTable.clearExclusiveMap();
		}

		return null;
	}
	@Override
	public Object visitClassDecl(ClassDecl cd, Integer arg) {
		//use sets to detect if any fields or methods are duplicate names
		//fields or methods with duplicate names are ignored and not visited
		HashSet<String> checkFieldsAndMethods = new HashSet<>();
		currentClass = cd;
		for(FieldDecl fd : cd.fieldDeclList) {
			if(checkFieldsAndMethods.contains(fd.name)) {
				addError("Error: Duplicate field name, ", fd.name, fd.posn);
			} else {
				checkFieldsAndMethods.add(fd.name);
				fd.visit(this, 0);
			}
		}
		for(MethodDecl md : cd.methodDeclList) {
			if(checkFieldsAndMethods.contains(md.name)) {
				addError("Error: Duplicate method name, ", md.name, md.posn);
			} else {
				checkFieldsAndMethods.add(md.name);
				md.visit(this, 0);
			}
		}

		checkFieldsAndMethods.clear();
		return null;
	}
	@Override
	public Object visitFieldDecl(FieldDecl fd, Integer arg) {
		fd.type.visit(this, 0);

		/*
		 * if(fd.type.typeKind == TypeKind.CLASS) { fd.fid.decl =
		 * visitClassType2((ClassType) fd.type); idTable.enterExclusiveMap(fd.fid,
		 * (ClassDecl) fd.fid.decl); }
		 */

		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl md, Integer arg){
		//this is for handling "main" method
		//public static void main(String[] args)
		if(md.name.equals("main")) {
			if(md.isPrivate) {
				addError("Public modifier is expected. Got private instead,", md.name, md.posn);
			}
			if(!md.isStatic) {
				addError("Static modifier is expected. Got not static instead, ", md.name, md.posn);
			}
			if(md.type.typeKind != TypeKind.VOID) {
				addError("Void type is expected. Did not get void instead, ", md.name, md.posn);
			}
			if(md.parameterDeclList.size() != 1) {
				addError("Incorrect number of parameters, , ", md.name, md.posn);
			}
		}

		md.type.visit(this, 0);

		isStatic = md.isStatic;

		idTable.openScope();

		//parsing the parameters
		HashSet<String> checkParameters = new HashSet<>();
		for(ParameterDecl pd : md.parameterDeclList) {
			if(checkParameters.contains(pd.name)) {
				addError("Error: Duplicate parameter name, ", pd.name, pd.posn);
			} else {
				checkParameters.add(pd.name);
				pd.visit(this, 0);
			}
		}
		checkParameters.clear();

		//parsing the statements
		for(Statement s : md.statementList) {
			s.visit(this, 0);
		}

		//possibly factor in a return statement??

		idTable.closeScope();
		isStatic = false;
		return null;
	}

	@Override
	public Object visitParameterDecl(ParameterDecl pd, Integer arg) {
		idTable.enterDecl(pd);
		pd.type.visit(this, 0);

		if(pd.type.typeKind == TypeKind.CLASS) {
			pd.pid.decl = visitClassType2((ClassType) pd.type);
			idTable.enterExclusiveMap(pd.pid, (ClassDecl) pd.pid.decl);
		}
		return null;
	}
	@Override
	public Object visitVarDecl(VarDecl decl, Integer arg) {
		if(idTable.enterDecl(decl) == 0) {
			addError("Error: Duplicate variable declaration found", decl.name, decl.posn);
		} else {
			idTable.enterDecl(decl);
		}
		decl.type.visit(this, 0);

		if(decl.type.typeKind == TypeKind.CLASS) {
			decl.varId.decl = visitClassType2((ClassType) decl.type);
			idTable.enterExclusiveMap(decl.varId, (ClassDecl) decl.varId.decl);
		}

		return null;
	}
	@Override
	public Object visitBaseType(BaseType type, Integer arg) {
		//do nothing, will be used later for type checking
		return null;
	}
	@Override
	public Object visitClassType(ClassType type, Integer arg) {
		if(type.className.spelling.equals("String")) {
			type.typeKind = TypeKind.UNSUPPORTED;
			return null;
		}

		String className = type.className.spelling;
		Declaration classDecl = idTable.retrieve(className);

		if(classDecl == null) {
			addError("Error: Class Type was expected", type.className.spelling, type.posn);
		} else {
			type.className.decl = classDecl;
		}

		return null;
	}

	public ClassDecl visitClassType2(ClassType type) {
		String className = type.className.spelling;
		ClassDecl classDecl = null;
		if(idTable.retrieveClass(className) instanceof ClassDecl) {
			classDecl = (ClassDecl) idTable.retrieveClass(className);
		} else if(!(idTable.retrieveClass(className) instanceof ParameterDecl) && !(idTable.retrieve(className) instanceof FieldDecl) ){
			addError("Particular class was not declared", className, type.posn);
		}
		return classDecl;
	}

	@Override
	public Object visitArrayType(ArrayType type, Integer arg) {
		type.eltType.visit(this, 0);
		return null;
	}
	@Override
	public Object visitBlockStmt(BlockStmt stmt, Integer arg) {
		idTable.openScope();
		for(Statement s : stmt.sl) {
			s.visit(this, 0);
		}
		idTable.closeScope();
		return null;
	}
	@Override
	public Object visitVardeclStmt(VarDeclStmt stmt, Integer arg) {
		stmt.varDecl.visit(this, 0);
		idTable.enterDecl(stmt.varDecl);
		stmt.initExp.visit(this, 0);

		//handles cases such as int x = x + y
		if(stmt.initExp instanceof BinaryExpr) {
			if(((BinaryExpr) stmt.initExp).left instanceof RefExpr && ((BinaryExpr) stmt.initExp).right instanceof RefExpr) {
				String declaredID = stmt.varDecl.varId.spelling;
				String leftID = ((RefExpr) ((BinaryExpr) stmt.initExp).left).ref.getID().spelling;
				String rightID = ((RefExpr) ((BinaryExpr) stmt.initExp).right).ref.getID().spelling;
				if(declaredID.equals(leftID) || declaredID.equals(rightID)) {
					addError("Variable declaration cannot reference variable being declared", declaredID, stmt.posn);
				}
			}
		}

		//handles cases such as Class y = x, where x is already a class object
		if(stmt.initExp instanceof RefExpr) {
			if(stmt.varDecl.type instanceof ClassType) {

				ClassDecl varDeclClass = idTable.returnExclusiveMap(stmt.varDecl.varId.spelling);
				ClassDecl tempClassDecl = idTable.returnExclusiveMap(((RefExpr) stmt.initExp).ref.getID().spelling);

				//handle cases where object of such as Class is not declared yet
				if(varDeclClass == null || tempClassDecl == null) {
					addError("Such class has not been declared yet ", stmt.varDecl.varId.spelling, stmt.posn);
				}

				if(!(varDeclClass.name.equals(tempClassDecl.name))) {
					addError("Class declaration mismatch between declared class " + varDeclClass.name + " and initialized class " + tempClassDecl.name, varDeclClass.name, stmt.posn);
				}
			}
		}

		//if(((RefExpr) stmt.initExp).ref.getID())
		//System.out.println(((BinaryExpr) stmt.initExp).);
		return null;
	}
	@Override
	public Object visitAssignStmt(AssignStmt stmt, Integer arg) {
		if(stmt.ref instanceof ThisRef) {
			addError("Error: 'this' cannot be used as the last component of the reference", "this", stmt.ref.posn);
		}
		stmt.ref.visit(this, 0);
		stmt.val.visit(this, 0);

		if(stmt.val instanceof RefExpr) {
			if(((RefExpr) stmt.val).ref instanceof IdRef) {
				String potentialClass = ((IdRef) ((RefExpr) stmt.val).ref).id.spelling;
				if(idTable.retrieve(potentialClass) instanceof ClassDecl) {
					addError("Reference to " + potentialClass + " does not denote a field or variable", potentialClass, stmt.posn);
				}
			}
		}

		//handles a.length = integer, where a is an array
		if(stmt.ref instanceof QualRef) {
			if(((QualRef) stmt.ref).ref instanceof IdRef && ((QualRef) stmt.ref).id.spelling.contentEquals("length")) {
				if(((IdRef) ((QualRef) stmt.ref).ref).decl.type instanceof ArrayType) {
					addError("Length reference for array is not assignable", ".length", stmt.posn);
				}
			}
		}

		return null;
	}
	@Override
	public Object visitIxAssignStmt(IxAssignStmt stmt, Integer arg) {
		if(stmt.ref instanceof ThisRef) {
			addError("Error: 'this' cannot be used as the last component of the reference", "this", stmt.ref.posn);
		}

		stmt.ref.visit(this, 0);
		stmt.ix.visit(this, 0);
		stmt.exp.visit(this, 0);
		return null;
	}
	@Override
	public Object visitCallStmt(CallStmt stmt, Integer arg) {
		//parse arguments
		stmt.methodRef.visit(this, 0);
		for(Expression e : stmt.argList) {
			e.visit(this, 0);
		}
		return null;
	}
	@Override
	public Object visitReturnStmt(ReturnStmt stmt, Integer arg) {
		if(stmt.returnExpr == null) {
			return null;
		} else {
			stmt.returnExpr.visit(this, 0);
		}
		return null;
	}
	@Override
	public Object visitIfStmt(IfStmt stmt, Integer arg) {
		//System.out.println("Visited if statement at " + stmt.posn.start);
		stmt.cond.visit(this, 0);

		if(stmt.thenStmt instanceof VarDeclStmt) {
			addError("Error : Variable Declaration statement not allowed inside the body of if statement body", stmt.thenStmt.toString(), stmt.thenStmt.posn);
		} else {
			stmt.thenStmt.visit(this, 0);
		}

		if(stmt.elseStmt != null) {
			if(stmt.elseStmt instanceof VarDeclStmt) {
				addError("Error: Variable Declaration statement not allowed inside then statement body", stmt.elseStmt.toString(), stmt.elseStmt.posn);
			} else {
				stmt.elseStmt.visit(this, 0);
			}
		}
		return null;
	}
	@Override
	public Object visitWhileStmt(WhileStmt stmt, Integer arg) {
		stmt.cond.visit(this, 0);

		if(stmt.body instanceof VarDeclStmt) {
			addError("Error: Variable Declaration statement not allowed inside a while body statement", stmt.cond.toString(), stmt.cond.posn);
		} else {
			stmt.body.visit(this, 0);
		}

		return null;
	}
	@Override
	public Object visitUnaryExpr(UnaryExpr expr, Integer arg) {
		expr.expr.visit(this, 0);
		return null;
	}
	@Override
	public Object visitBinaryExpr(BinaryExpr expr, Integer arg) {
		expr.left.visit(this, 0);
		expr.right.visit(this, 0);
		return null;
	}
	@Override
	public Object visitRefExpr(RefExpr expr, Integer arg) {
		expr.ref.visit(this, 0);
		return null;
	}
	@Override
	public Object visitIxExpr(IxExpr expr, Integer arg) {
		expr.ref.visit(this, 0);
		expr.ixExpr.visit(this, 0);
		return null;
	}
	@Override
	public Object visitCallExpr(CallExpr expr, Integer arg) {
		expr.functionRef.visit(this, 0);

		//for method calls such as int x = foo(20); where foo is a method in the same class
		if(expr.functionRef instanceof IdRef) {
			Declaration tempDecl = idTable.retrieve(((IdRef) expr.functionRef).id.spelling);
			if(tempDecl instanceof MethodDecl) {
				((IdRef) expr.functionRef).id.decl = tempDecl;
			}
		}

		for(Expression e : expr.argList) {
			e.visit(this, 0);
		}
		return null;
	}
	@Override
	public Object visitLiteralExpr(LiteralExpr expr, Integer arg) {
		expr.lit.visit(this, 0);
		return null;
	}
	@Override
	public Object visitNewObjectExpr(NewObjectExpr expr, Integer arg) {
		expr.classtype.visit(this, 0);
		return null;
	}
	@Override
	public Object visitNewArrayExpr(NewArrayExpr expr, Integer arg) {
		expr.eltType.visit(this, 0);
		expr.sizeExpr.visit(this, 0);
		return null;
	}
	@Override
	public Object visitThisRef(ThisRef ref, Integer arg) {
		if(isStatic) {
			addError("Error: Cannot use this and static at the same time", "this", ref.posn);
		}
		ref.decl = currentClass;
		return null;
	}
	@Override
	public Object visitIdRef(IdRef ref, Integer arg) {
		ref.decl = idTable.retrieve(ref.id.spelling);
		ref.id.visit(this, 0);
		if(ref.id.decl instanceof MemberDecl) {
			MemberDecl memberDecl = (MemberDecl) ref.id.decl;
			ClassDecl classDecl = memberDecl.classDecl;
			if(classDecl != currentClass) {
				addError("Error: Cannot find the reference in the current class", ref.id.spelling, ref.posn);
			}
		}
		return null;
	}
	@Override
	public Object visitQRef(QualRef ref, Integer arg) {
		//A QualRef will have an Id and a Reference, and the Reference can be a QualRef.
		//So your QualRef can have QualRefs until it bottoms out with an IdRef or a ThisRef

		String ref_name = ref.id.spelling;
		ref.ref.visit(this, 0);
		Declaration ref_decl = ref.ref.decl;

		if(!(ref.ref instanceof QualRef)) {
				if(ref_decl.type instanceof ClassType) {
					String class_name = ref_decl.name;

					ClassDecl classDecl = idTable.returnExclusiveMap(class_name);

					if(classDecl == null) {
						//nested fields such as n.next.next.next.next where next is a field
						addError("Class or method does not exist", class_name, ref.posn);
					}
					HashMap<String, Declaration> memberTable = classDecl.initiateHashMap();

					if(memberTable.containsKey(ref_name)) {
						MemberDecl memberDecl = (MemberDecl) memberTable.get(ref_name);
						if(memberDecl.isPrivate) {
							if(currentClass.name.equals(classDecl.name)) {
								ref.decl = memberDecl;
							} else {
								addError("Cannot access a private member of another class", class_name, memberDecl.posn);
							}
						} else {
							ref.decl = memberDecl;
						}
					} else {
						addError("Class does not contain the member", classDecl.name, ref.posn);
					}
				} else if(ref.ref.getDecl() instanceof ClassDecl) {
					ClassDecl tempClass = (ClassDecl) ref.ref.getDecl();
					String class_name = ref_decl.name;
					HashMap<String, Declaration> memberTable = ((ClassDecl) ref_decl).initiateHashMap();

					if(memberTable.containsKey(ref_name)) {
						MemberDecl memberDecl = (MemberDecl) memberTable.get(ref_name);

						if(memberDecl.isPrivate) {
							if(currentClass.equals(ref_decl)) {
								ref.decl = memberDecl;
							} else {
								addError("Cannot access a private member of another class", class_name, memberDecl.posn);
							}
						} else if(!memberDecl.isStatic) {
							if(ref.ref instanceof IdRef) {
								if(((IdRef) ref.ref).id.spelling.equals(class_name)) {
									addError("Class can only access static methods", class_name, memberDecl.posn);
								}
							}
							ref.decl = memberDecl;
						} else {
							ref.decl = memberDecl;
						}
				}
			} else if(ref.ref instanceof ThisRef) {
				//referring to the current class
				HashMap<String, Declaration> memberTable = currentClass.initiateHashMap();
				if(memberTable.containsKey(ref_name)) {
					MemberDecl memberDecl = (MemberDecl) memberTable.get(ref_name);
					ref.decl = memberDecl;
				}
			} else if(ref.ref.decl.type.typeKind == TypeKind.ARRAY) {
				if(ref.id.spelling.equals("length")) {
					ref.decl = new FieldDecl(false, false, new BaseType(TypeKind.INT, null), "length", null);
				}
			} else {
				addError("Cannot have method in the middle of a qualified reference", ref.id.spelling, ref.posn);
			}
		} else {
			QualRef tempQualRef = (QualRef) ref.ref;

			//specifically for system.out.println
			String class_name = tempQualRef.id.spelling;
			//System.out.println(tempQualRef.id.spelling);
			if(idTable.returnExclusiveMap(tempQualRef.id.spelling) instanceof ClassDecl) {
				HashMap<String, Declaration> memberTable = ((ClassDecl) idTable.returnExclusiveMap(tempQualRef.id.spelling)).initiateHashMap();
				//System.out.println(tempQualRef.id.spelling);
				if(memberTable.containsKey(ref_name)) {
					MemberDecl memberDecl = (MemberDecl) memberTable.get(ref_name);

					if(memberDecl.isPrivate) {
						String tempClassName = idTable.returnExclusiveMap(ref_decl.name).name;
						if(currentClass.name.equals(tempClassName)) {
							ref.decl = memberDecl;
						} else {
							addError("Cannot access a private member of another class", class_name, memberDecl.posn);
						}
					} else if(!memberDecl.isStatic) {
						if(ref.ref instanceof IdRef) {
							if(((IdRef) ref.ref).id.spelling.equals(class_name)) {
								addError("Class can only access static methods", class_name, memberDecl.posn);
							}
						}
						ref.decl = memberDecl;
					} else {
						ref.decl = memberDecl;
					}
			}
			} else {
				addError("Cannot find specific reference", class_name, tempQualRef.posn);
			}
		}

		return ref;
	}
	@Override
	public Object visitIdentifier(Identifier id, Integer arg) {
		Declaration idDecl = idTable.retrieve(id.spelling);

		if(idDecl == null) {
			addError("Undeclared variable", id.spelling, id.posn);
		} else {
			if(idDecl instanceof MemberDecl) {
				MemberDecl memberDecl = (MemberDecl) idDecl;
				if(isStatic && !memberDecl.isStatic) {
					addError("Cannot reference non-static variables inside a static method", memberDecl.name, memberDecl.posn);
				}
				idDecl.type.visit(this, 0);
			} else if(idDecl instanceof LocalDecl) {
				id.isLocal = true;
				idDecl.type.visit(this, 0);
			} else if(idDecl instanceof ClassDecl) {
				id.decl = idDecl;
			}
		}
		return null;
	}
	@Override
	public Object visitOperator(Operator op, Integer arg) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object visitIntLiteral(IntLiteral num, Integer arg) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Object visitBooleanLiteral(BooleanLiteral bool, Integer arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitNullLiteral(NullLiteral nullL, Integer arg) {
		// TODO Auto-generated method stub
		//possibly add an error here or do this during type checking??
		return null;
	}

}
