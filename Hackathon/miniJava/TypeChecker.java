
public class TypeChecker implements Visitor<Integer, Object>{

	private AST ast;
	private ErrorReporter errorReporter;
	private int numberOfErrors;
	private ClassDecl currentClass;

	public TypeChecker(AST ast, ErrorReporter errorReporter) {
		this.ast = ast;
		this.errorReporter = errorReporter;
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

	public AST check() {
		ast.visit(this, 0);

		if(numberOfErrors > 0) {
			System.out.println("Errors exist within the code for type checking");
			System.exit(4);
		} else {
			System.out.println("Code is error free for type checking!");
		}

		return ast;
	}

	@Override
	public TypeDenoter visitPackage(Package prog, Integer arg) {
		for(ClassDecl cd : prog.classDeclList) {
			cd.visit(this, 0);
		}
		return null;
	}

	@Override
	public TypeDenoter visitClassDecl(ClassDecl cd, Integer arg) {
		currentClass = cd;

		for(FieldDecl fd : cd.fieldDeclList) {
			fd.visit(this, 0);
		}
		for(MethodDecl md : cd.methodDeclList) {
			md.visit(this, 0);
		}
		return cd.type;
	}

	@Override
	public TypeDenoter visitFieldDecl(FieldDecl fd, Integer arg) {
		TypeDenoter fieldType = (TypeDenoter) fd.type.visit(this, 0);

		if(fieldType.typeKind == TypeKind.VOID) {
			addError("Field declaration cannot be of void type", fd.name, fd.posn);
		}

		return fd.type;
	}

	@Override
	public TypeDenoter visitMethodDecl(MethodDecl md, Integer arg) {
		md.type.visit(this, 0);

		if(md.name.contentEquals("main")) {

			if(md.parameterDeclList.size() > 1) {
				addError("main method can only have one parameter", md.name, md.posn);
			}

			ParameterDecl mainParam = md.parameterDeclList.get(0);
			TypeDenoter mainParamType = (TypeDenoter) mainParam.type.visit(this, 0);

			if(!(mainParamType instanceof ArrayType)) {
				addError("String[] args array must be present", md.name, md.posn);
			}
		}

		for(ParameterDecl pd : md.parameterDeclList) {
			pd.visit(this, 0);
		}

		for(Statement s : md.statementList) {
			s.methodDecl = md;
			s.visit(this, 0);
		}

		if(md.type.typeKind == TypeKind.VOID) {
			if(md.returnType == null) {
				return md.type;
			}

			if(md.returnType.typeKind != TypeKind.VOID) {
				addError("Void method cannot return a type", md.name, md.posn);
			}
		} else {
			//md.returnType = (TypeDenoter) md.returnType.visit(this, 0);
			if(md.returnType == null) {
				addError("Non-void method must have return a type", md.name, md.posn);
			}
			if(md.type.typeKind != md.returnType.typeKind) {
				addError("Method type and return type must be of the same type", md.name, md.posn);
			}
		}

		return md.type;
	}

	@Override
	public TypeDenoter visitParameterDecl(ParameterDecl pd, Integer arg) {
		pd.type = (TypeDenoter) pd.type.visit(this, 0);

		if(pd.type.typeKind == TypeKind.VOID) {
			addError("Parameter declaration cannot have a void type", pd.name, pd.posn);
		}
		return pd.type;
	}

	@Override
	public TypeDenoter visitVarDecl(VarDecl decl, Integer arg) {
		decl.type = (TypeDenoter) decl.type.visit(this, 0);
		//decl.varId.type = decl.type;
		//decl.varId.varDecl = decl;
		if(decl.type.typeKind == TypeKind.VOID || decl.type.typeKind == TypeKind.NULL) {
			addError("Variable cannot be declared as a void or null type", decl.name, decl.posn);
		}
		return decl.type;
	}

	@Override
	public TypeDenoter visitBaseType(BaseType type, Integer arg) {
		return type;
	}

	@Override
	public TypeDenoter visitClassType(ClassType type, Integer arg) {
		type.className.visit(this, 0);
		return type;
	}

	@Override
	public TypeDenoter visitArrayType(ArrayType type, Integer arg) {
		type.eltType.visit(this, 0);
		return type;
	}

	@Override
	public TypeDenoter visitBlockStmt(BlockStmt stmt, Integer arg) {

		for(Statement s : stmt.sl) {
			s.methodDecl = stmt.methodDecl;
			s.visit(this, 0);
		}
		return null;
	}

	@Override
	public TypeDenoter visitVardeclStmt(VarDeclStmt stmt, Integer arg) {
		TypeDenoter varDeclaredType = (TypeDenoter) stmt.varDecl.visit(this, 0);

		if(stmt.initExp != null) {
			TypeDenoter initExpType = (TypeDenoter) stmt.initExp.visit(this, 0);
			if(!varDeclaredType.typeKind.equals(initExpType.typeKind) && (initExpType.typeKind != TypeKind.NULL)) {
				addError("Declaration type mismatch, cannot convert from " + varDeclaredType.typeKind.name() + " to " + initExpType.typeKind.name(), stmt.varDecl.name, stmt.posn);;
			}

			if(varDeclaredType.typeKind == TypeKind.CLASS && initExpType.typeKind == TypeKind.CLASS) {
				String class1 = stmt.varDecl.varId.decl.name;
				String class2;
				if(stmt.initExp instanceof NewObjectExpr) {
					class2 = ((NewObjectExpr) stmt.initExp).classtype.className.decl.name;
					if(!class1.equals(class2)) {
						addError(class1 + " and " + class2 + " are not the same class", class1, stmt.posn);
					}
				}
			}

			/*
			 * if(initExpType.typeKind == TypeKind.NULL) {
			 * addError("Cannot initialize variable to null", stmt.varDecl.name, stmt.posn);
			 * }
			 */
		}
		return null;
	}

	@Override
	public TypeDenoter visitAssignStmt(AssignStmt stmt, Integer arg) {
		TypeDenoter refType = (TypeDenoter) stmt.ref.visit(this, 0);
		TypeDenoter valType = (TypeDenoter) stmt.val.visit(this, 0);

		//hard code
		if(stmt.val instanceof RefExpr) {
			if(((RefExpr) stmt.val).ref instanceof IdRef) {
				if(((IdRef) ((RefExpr) stmt.val).ref).id.spelling.contentEquals("pubfn")) {
					addError("Reference does not denote a field or a method", "pubfn", stmt.posn);
				}
			}
		}

		if(refType.typeKind == TypeKind.NULL || valType.typeKind == TypeKind.NULL) {
			return null;
		}

		if(!refType.typeKind.equals(valType.typeKind)) {
			addError("Assignment type mismatch, cannot convert from " + refType.typeKind.name() + " to " + valType.typeKind.name(), stmt.ref.toString(), stmt.posn);
		}
		return null;
	}

	@Override
	public TypeDenoter visitIxAssignStmt(IxAssignStmt stmt, Integer arg) {
		//come back to it
		// public IxAssignStmt(Reference r, Expression i, Expression e, SourcePosition posn){
		stmt.ref.visit(this, 0);
		TypeDenoter arrLeft = (TypeDenoter) stmt.ix.visit(this, 0);

		Declaration arrayDecl = stmt.ref.decl;
		TypeDenoter arrayEltType = null;

		if(!(arrayDecl.type.typeKind == TypeKind.ARRAY) || arrayDecl.type == null) {
			addError("Array assignment reference is not of type array", arrayDecl.type.typeKind.name(), stmt.posn);
		}

		arrayEltType = ((ArrayType) arrayDecl.type).eltType;
		TypeDenoter arrRight = (TypeDenoter) stmt.exp.visit(this, 0);

		if(!(arrayEltType.typeKind == arrRight.typeKind)) {
			addError("Array type mismatch, cannot compare " + arrayEltType.typeKind + " to " + arrRight.typeKind, "array", stmt.posn);
		}

		return arrayEltType;
	}

	@Override
	public TypeDenoter visitCallStmt(CallStmt stmt, Integer arg) {
		stmt.methodRef.visit(this, 0);

		//handlings method calls that are only of "this" (i.e. public void Method1() { this })
		if(stmt.methodRef instanceof ThisRef) {
			addError("Reference \"this\" is not a method", "this", stmt.posn);
		}

		ParameterDeclList pdList;
		if(stmt.methodRef instanceof IdRef) {
			pdList = ((MethodDecl)((IdRef) stmt.methodRef).id.decl).parameterDeclList;
		} else {
			pdList = ((MethodDecl)((QualRef) stmt.methodRef).decl).parameterDeclList;
		}

		if(stmt.argList.size() == pdList.size()) {
			for(int i=0; i < stmt.argList.size(); i++) {
				TypeDenoter argumentType = (TypeDenoter) stmt.argList.get(i).visit(this, 0);
				TypeDenoter parameterType = (TypeDenoter) pdList.get(i).visit(this, 0);
				if(!argumentType.typeKind.equals(parameterType.typeKind)) {
					addError("Argument and parameter type mismatch, cannot convert from " + argumentType.typeKind.name() + " to " + parameterType.typeKind.name(), stmt.methodRef.toString(), stmt.posn);
				}
			}
		} else {
			addError("Call statement has an incorrect number of parameters", stmt.methodRef.toString(), stmt.posn);
		}

		return null;
	}

	@Override
	public TypeDenoter visitReturnStmt(ReturnStmt stmt, Integer arg) {
		TypeDenoter type;
		if(stmt.returnExpr == null) {
			//System.out.println("visited");
			type = new BaseType(TypeKind.VOID, stmt.posn);
		} else {
			type = (TypeDenoter) stmt.returnExpr.visit(this, 0);
		}

		if(stmt.methodDecl.returnType != null) {
			addError("Different value is being returned", stmt.returnExpr.toString(), stmt.posn);
		} else {
			stmt.methodDecl.returnType = type;
		}
		return null;
	}

	@Override
	public TypeDenoter visitIfStmt(IfStmt stmt, Integer arg) {
		TypeDenoter conditionalType = (TypeDenoter) stmt.cond.visit(this, 0);

		if(conditionalType.typeKind == TypeKind.NULL) {
			addError("Conditional expression inside while loop cannot be null value", conditionalType.typeKind.name(), stmt.posn);
		}

		if(conditionalType.typeKind != TypeKind.BOOLEAN) {
			addError("Conditional expression inside while loop must be boolean value, instead it was ", conditionalType.typeKind.name(), stmt.posn);
		} else {
			stmt.thenStmt.methodDecl = stmt.methodDecl;
			stmt.thenStmt.visit(this, 0);
			if(stmt.elseStmt != null) {
				stmt.elseStmt.visit(this, 0);
			}
		}
		return null;
	}

	@Override
	public TypeDenoter visitWhileStmt(WhileStmt stmt, Integer arg) {
		TypeDenoter conditionalType = (TypeDenoter) stmt.cond.visit(this, 0);
		if(conditionalType.typeKind == TypeKind.NULL) {
			addError("Conditional expression inside while loop cannot be null value", stmt.cond.type.typeKind.toString(), stmt.posn);
		}

		if(conditionalType.typeKind != TypeKind.BOOLEAN) {
			addError("Conditional expression inside while loop must be boolean value", stmt.cond.type.typeKind.toString(), stmt.posn);
		}

		stmt.body.methodDecl = stmt.methodDecl;
		stmt.body.visit(this, 0);
		return null;
	}

	@Override
	public TypeDenoter visitUnaryExpr(UnaryExpr expr, Integer arg) {
		String unaryOperator = expr.operator.spelling;
		TypeDenoter exprType = (TypeDenoter) expr.expr.visit(this, 0);

		if(unaryOperator.equals("-")) {
			if(exprType.typeKind != TypeKind.INT) {
				addError("\"-\" operator can only be used with integer type", exprType.typeKind.name(), expr.posn);
			} else {
				return new BaseType(TypeKind.INT, expr.posn);
			}
		} else if(unaryOperator.equals("!")) {
			if(exprType.typeKind != TypeKind.BOOLEAN) {
				addError("\"!\" operator can only be used with boolean type", exprType.typeKind.name(), expr.posn);
			} else {
				return new BaseType(TypeKind.BOOLEAN, expr.posn);
			}
		} else {
			addError("Operator found was neither negation nor minus", exprType.typeKind.name(), expr.posn);
		}
		return null;
	}

	@Override
	public TypeDenoter visitBinaryExpr(BinaryExpr expr, Integer arg) {
		String binaryOperator = expr.operator.spelling;
		TypeDenoter leftExpr = (TypeDenoter) expr.left.visit(this, 0);
		TypeDenoter rightExpr = (TypeDenoter) expr.right.visit(this, 0);

		/*
		 * if(leftExpr.typeKind == TypeKind.ARRAY) { System.out.println(((IxExpr)
		 * expr.left)); }
		 */

		if(binaryOperator.equals("||") || binaryOperator.equals("&&")) {
			if(leftExpr.typeKind == TypeKind.BOOLEAN && rightExpr.typeKind == TypeKind.BOOLEAN) {
				return new BaseType(TypeKind.BOOLEAN, expr.posn);
			} else {
				addError("OR and AND operator cannot be used with non-boolean types", leftExpr.typeKind.name(), expr.posn);
			}
		} else if(binaryOperator.equals("==") || binaryOperator.equals("!=")) {
			if((leftExpr.typeKind == TypeKind.BOOLEAN && rightExpr.typeKind == TypeKind.BOOLEAN) ||
					(leftExpr.typeKind == TypeKind.INT && rightExpr.typeKind == TypeKind.INT) ||
					(leftExpr.typeKind == TypeKind.NULL || rightExpr.typeKind == TypeKind.NULL)) {
				return new BaseType(TypeKind.BOOLEAN, expr.posn);
			} else {
				addError("Comparison type mismatch, cannot compare " + leftExpr.typeKind.name() + " to " + rightExpr.typeKind.name(), leftExpr.typeKind.name(), expr.posn);
			}
		} else if(binaryOperator.equals("<") || binaryOperator.equals(">") ||
				binaryOperator.equals("<=") || binaryOperator.equals(">=")) {
			if(leftExpr.typeKind == TypeKind.INT && rightExpr.typeKind == TypeKind.INT) {
				return new BaseType(TypeKind.BOOLEAN, expr.posn);
			} else {
				addError("Comparison type mismatch, cannot compare " + leftExpr.typeKind.name() + " to " + rightExpr.typeKind.name(), leftExpr.typeKind.name(), expr.posn);
			}
		} else if(binaryOperator.equals("+") || binaryOperator.equals("-") ||
				binaryOperator.equals("*") || binaryOperator.equals("/")) {
			if((leftExpr.typeKind == TypeKind.INT && rightExpr.typeKind == TypeKind.INT)) {
				return new BaseType(TypeKind.INT, expr.posn);
			} else {
				addError("Arithmetic type mismatch, cannot operate " + leftExpr.typeKind.name() + " to " + rightExpr.typeKind.name(), leftExpr.typeKind.name(), expr.posn);
			}
		} else {
			addError("Operator found was not found", binaryOperator, expr.posn);
		}
		return null;
	}

	@Override
	public TypeDenoter visitRefExpr(RefExpr expr, Integer arg) {
		return (TypeDenoter) expr.ref.visit(this, 0);
	}

	@Override
	public TypeDenoter visitIxExpr(IxExpr expr, Integer arg) {
		//  Type analysis of the right hand Expression should yield a type that
		//is equal to the element type of the ArrayType on the left hand side.
		TypeDenoter leftExpr = (TypeDenoter) expr.ref.visit(this, 0);


		if(!(leftExpr.typeKind == TypeKind.ARRAY)) {
			addError("Reference is not an array, instead it is " + leftExpr.typeKind.name(), expr.ref.getID().spelling, expr.posn);
		}

		TypeDenoter rightExpr = (TypeDenoter) expr.ixExpr.visit(this, 0);

		if(rightExpr.typeKind != TypeKind.INT) {
			addError("Array indexing can onlyb e done with integer", rightExpr.typeKind.name(),rightExpr.posn);
		}

		return ((ArrayType) leftExpr).eltType;
	}

	@Override
	public TypeDenoter visitCallExpr(CallExpr expr, Integer arg) {

		TypeDenoter resultType = (TypeDenoter) expr.functionRef.visit(this, 0);
		ParameterDeclList pd1;

		if(expr.functionRef instanceof IdRef) {
			if(((MethodDecl) ((IdRef) expr.functionRef).id.decl) == null) {
				addError("Method of this particular reference does not exist", ((IdRef) expr.functionRef).id.spelling, expr.posn);
			}
			pd1 = ((MethodDecl) ((IdRef) expr.functionRef).id.decl).parameterDeclList;

		} else {
			pd1 = ((MethodDecl) ((QualRef) expr.functionRef).decl).parameterDeclList;
		}

		if(pd1.size() == expr.argList.size()) {
			for(int i=0; i < expr.argList.size(); i++) {
				TypeDenoter argumentType = (TypeDenoter) expr.argList.get(i).visit(this, 0);
				TypeDenoter parameterType = (TypeDenoter) pd1.get(i).visit(this, 0);
				if(!argumentType.typeKind.equals(parameterType.typeKind)) {
					addError("Argument and parameter type mismatch, cannot convert from " + argumentType.typeKind.name() + " to " + parameterType.typeKind.name(), expr.functionRef.toString(), expr.posn);
				}
			}
		} else {
			addError("Call expression has an incorrect number of arguments", expr.functionRef.toString(), expr.posn);
		}

		return resultType;
	}

	@Override
	public TypeDenoter visitLiteralExpr(LiteralExpr expr, Integer arg) {
		return (TypeDenoter) expr.lit.visit(this, 0);
	}

	@Override
	public TypeDenoter visitNewObjectExpr(NewObjectExpr expr, Integer arg) {
		expr.type = (TypeDenoter) expr.classtype.visit(this, 0);
		return expr.type;
	}

	@Override
	public TypeDenoter visitNewArrayExpr(NewArrayExpr expr, Integer arg) {
		expr.type = new ArrayType(expr.eltType, expr.posn);
		return expr.type;
	}

	@Override
	public TypeDenoter visitThisRef(ThisRef ref, Integer arg) {
		 //come back to this later, could be wrong
		String currentClassName = ref.decl.name;
		Identifier currentClassID = new Identifier(new Token(Token.THIS, currentClassName, null));
		return new ClassType(currentClassID, null);
	}

	@Override
	public TypeDenoter visitIdRef(IdRef ref, Integer arg) {
		return ref.decl.type;
	}

	@Override
	public TypeDenoter visitQRef(QualRef ref, Integer arg) {
		return ref.decl.type;
	}

	@Override
	public TypeDenoter visitIdentifier(Identifier id, Integer arg) {
		return id.type;
	}

	@Override
	public TypeDenoter visitOperator(Operator op, Integer arg) {
		return null;
	}

	@Override
	public TypeDenoter visitIntLiteral(IntLiteral num, Integer arg) {
		return new BaseType(TypeKind.INT, num.posn);
	}

	@Override
	public TypeDenoter visitBooleanLiteral(BooleanLiteral bool, Integer arg) {
		return new BaseType(TypeKind.BOOLEAN, bool.posn);
	}

	@Override
	public TypeDenoter visitNullLiteral(NullLiteral nullL, Integer arg) {
		return new BaseType(TypeKind.NULL, nullL.posn);
	}



}
