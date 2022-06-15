
public class Parser {

	private Scanner analyzer;
	private Token currentToken;
	private SourcePosition position_token;
	private ErrorReporter errorReporter;

	public Parser(Scanner scanner, ErrorReporter reporter) {
		this.analyzer = scanner;
		this.position_token = new SourcePosition();
		this.errorReporter = reporter;
	}

	//accept will proceed to scan the next token
	void accept(int tokenExpected) {
		if(currentToken.kind == tokenExpected) {
			position_token = currentToken.position;
			currentToken = analyzer.scan();
		} else {
			syntaxError(Token.getKind(tokenExpected), Token.getKind(currentToken.kind), position_token);
		}
	}

	//guaranteed that token is already correct
	void acceptIt() {
	    position_token = currentToken.position;
	    currentToken = analyzer.scan();
	}

	void acceptString(int tokenExpected) {
		if(currentToken.kind == tokenExpected) {
			position_token = currentToken.position;
			currentToken = analyzer.scanString();
		} else {
			syntaxError(Token.getKind(tokenExpected), Token.getKind(currentToken.kind), position_token);
		}
	}

	void start(SourcePosition posn) {
		posn.start = currentToken.position.start;
	}

	void finish(SourcePosition posn) {
		posn.finish = position_token.finish;
	}

	void syntaxError(String expectedToken, String actualToken, SourcePosition tokenPosition) {
		String errorMessage = "Expected " + expectedToken + ", Got " + actualToken + " instead. The code with error was " + currentToken.spelling + " between the lines "
				+ tokenPosition.start + " and " + tokenPosition.finish;
		System.out.println(errorMessage);
		errorReporter.reportError(errorMessage);
		System.exit(4);
	}

	private void parseError(String str) {
		errorReporter.reportError("Parse Error: " + str);
		System.exit(4);
	}

	//begin the program parsing, compiler will create Parse object and call this method
	//Program  ::=  (ClassDeclaration)* eot
	public Package parseProgram() {
		position_token.start = 0;
		position_token.finish = 0;
		currentToken = analyzer.scan();
		SourcePosition pos = new SourcePosition();
		ClassDeclList classDeclList = new ClassDeclList();
		start(pos);
	  String documentString = "---";

		//scans for all the different classes until EOT
		while(currentToken.kind != Token.EOT) {
			SourcePosition classPosn = new SourcePosition();
			start(classPosn);

			MethodDeclList methodDeclList = new MethodDeclList();
            FieldDeclList fieldDeclList = new FieldDeclList();

            accept(Token.CLASS);

            String className = currentToken.spelling;
            accept(Token.IDENTIFIER);

            accept(Token.LCURLY);

            while(currentToken.kind != Token.RCURLY) {
							if(currentToken.kind == Token.QUOTATION) {
								acceptIt();
								accept(Token.QUOTATION);
								acceptString(Token.QUOTATION);
								documentString = currentToken.spelling;
								acceptIt();
								accept(Token.QUOTATION);
								accept(Token.QUOTATION);
								accept(Token.QUOTATION);
							}

            	SourcePosition withClassPosn = new SourcePosition();
            	start(withClassPosn);

            	boolean isPublic = true;
            	boolean isPrivate = true;
            	if(currentToken.kind == Token.PRIVATE) {
            		acceptIt();
            		isPublic = false;
            	}

            	if(currentToken.kind == Token.PUBLIC) {
            		acceptIt();
            		isPrivate = false;
            	}

            	boolean isStatic = false;
            	if(currentToken.kind == Token.STATIC) {
            		acceptIt();
            		isStatic = true;
            	}

            	//if void this indicates it is a method declaration
            	if(currentToken.kind == Token.VOID) {
            		TypeDenoter typeDenoter = new BaseType(TypeKind.VOID, currentToken.position);
            		acceptIt();
            		Identifier methodName = new Identifier(currentToken);
            		accept(Token.IDENTIFIER);
            		methodDeclList.add(parseMethod(isPublic, isStatic, typeDenoter, methodName, withClassPosn));
            	} else {
            		//find type whether it be int, boolean, array etc
            		TypeDenoter typeDenoter = parseType();
            		Identifier identifier = new Identifier(currentToken);
            		accept(Token.IDENTIFIER);

            		//confirms there will be parameters
            		if(currentToken.kind != Token.SEMICOLON) {
            			methodDeclList.add(parseMethod(isPublic, isStatic, typeDenoter, identifier, withClassPosn));
            		} else {
									String fieldDocsString = "---";
            			//confirms it is field a declaration
            			acceptIt();
            			finish(withClassPosn);

									if(currentToken.kind == Token.QUOTATION) {
										acceptIt();
										accept(Token.QUOTATION);
										acceptString(Token.QUOTATION);
										fieldDocsString = currentToken.spelling;
										acceptIt();
										accept(Token.QUOTATION);
										accept(Token.QUOTATION);
										accept(Token.QUOTATION);
									}

            			fieldDeclList.add(new FieldDecl(!isPublic, isStatic, typeDenoter, identifier.spelling, identifier, withClassPosn, fieldDocsString));
            		}
            	}
            }
            accept(Token.RCURLY);
            finish(classPosn);
            classDeclList.add(new ClassDecl(className, fieldDeclList, methodDeclList, classPosn, documentString));
		}

		//program finishes
		accept(Token.EOT);
		finish(pos);
		return new Package(classDeclList, pos);
	}

	//continuation of class declaration specifically for methods
	private MethodDecl parseMethod(boolean isPublic, boolean isStatic, TypeDenoter type, Identifier id, SourcePosition pos) {
		ParameterDeclList parameterDeclList = new ParameterDeclList();
		String methodDocsString = "---";

		accept(Token.LPAREN);
		//no parameters
		if(currentToken.kind == Token.RPAREN) {
			acceptIt();
		} else {
			//parameters present
			parameterDeclList = parseParameters();
			accept(Token.RPAREN);
		}

		//statements begin after parameters end
		StatementList stmtList = new StatementList();
		accept(Token.LCURLY);

		if(currentToken.kind == Token.QUOTATION) {
			acceptIt();
			accept(Token.QUOTATION);
			acceptString(Token.QUOTATION);
			methodDocsString = currentToken.spelling;
			acceptIt();
			accept(Token.QUOTATION);
			accept(Token.QUOTATION);
			accept(Token.QUOTATION);
		}

		while(currentToken.kind != Token.RCURLY) {
			stmtList.add(parseStatement());
		}
		accept(Token.RCURLY);

		finish(pos);
		return new MethodDecl(new FieldDecl(!isPublic, isStatic, type, id.spelling, id, pos), parameterDeclList, stmtList, pos, methodDocsString);
	}

	public void parseID() {
		if(currentToken.kind == Token.IDENTIFIER) {
			acceptIt();
		} else {
			syntaxError(Token.getKind(Token.IDENTIFIER), Token.getKind(currentToken.kind), position_token);
		}
	}

	//Type  ::=  int  |  boolean  |  id  |  ( int | id ) []
	public TypeDenoter parseType() {
		SourcePosition typePosition = new SourcePosition();
		start(typePosition);

		switch(currentToken.kind) {
		case Token.BOOLEAN:
			acceptIt();
			finish(typePosition);
			return new BaseType(TypeKind.BOOLEAN, typePosition);

		//identifier and int can be of type id or of type int
		case Token.IDENTIFIER:
			Token id = currentToken;
			accept(Token.IDENTIFIER);
			if(currentToken.kind == Token.LBRACKET) {
				acceptIt();
				accept(Token.RBRACKET);
				finish(typePosition);
				return new ArrayType(new ClassType(new Identifier(id), typePosition), typePosition);
			} else {
				finish(typePosition);
				return new ClassType(new Identifier(id), typePosition);
			}

		case Token.INT:
			acceptIt();
			if(currentToken.kind == Token.LBRACKET) {
				acceptIt();
				accept(Token.RBRACKET);
				finish(typePosition);
				return new ArrayType(new BaseType(TypeKind.INT, typePosition), typePosition);
			} else {
				finish(typePosition);
				return new BaseType(TypeKind.INT, typePosition);
			}

		default:
			syntaxError("int, boolean or array", Token.getKind(currentToken.kind), position_token);
			finish(typePosition);
			return new BaseType(TypeKind.ERROR, typePosition);
		}
	}

	//parse return expression separately than other expressions because
	//exclusive only to certain classes
	public void parseReturnExpression() {
		acceptIt();
		parseExpression();
		accept(Token.SEMICOLON);
	}

	//public class(parameter 1, parameter 2, parameter 3.....)
	//ParameterList  ::=  Type id ( , Type  id )*
	public ParameterDeclList parseParameters() {
		ParameterDeclList parameterDeclList = new ParameterDeclList();
		SourcePosition parameterPos = new SourcePosition();
		start(parameterPos);

		//parse first parameter
		TypeDenoter typeDenoter = parseType();
		Identifier firstIdentifier = new Identifier(currentToken);
		accept(Token.IDENTIFIER);
		finish(parameterPos);
		parameterDeclList.add(new ParameterDecl(typeDenoter, firstIdentifier.spelling, firstIdentifier, parameterPos));

		while(currentToken.kind == Token.COMMA) {
			acceptIt();
			start(parameterPos);
			typeDenoter = parseType();
			Identifier laterIdentifier = new Identifier(currentToken);
			accept(Token.IDENTIFIER);
			finish(parameterPos);
			parameterDeclList.add(new ParameterDecl(typeDenoter, laterIdentifier.spelling, laterIdentifier, parameterPos));
		}
		return parameterDeclList;
	}

	//ArgumentList  ::=  Expression ( , Expression )*
	public ExprList parseArguments() {
		ExprList expressionList = new ExprList();
		expressionList.add(parseExpression());
		while(currentToken.kind == Token.COMMA) {
			acceptIt();
			expressionList.add(parseExpression());
		}

		return expressionList;
	}


//-----------------------------------------------------------------------
//Statements BLock


	public Statement parseStatement() {
		//AssignStmt CHECK(this) CHECK(this)
		//BlockStmt CHECK
		//CallStmt CHECK
		//IfStmt CHECK
		//IxAssignStmt CHECK
		//ReturnStmt CHECK
		//VarDeclStmt CHECK
		//WhileStmt CHECK

		SourcePosition currentPos = new SourcePosition();
		start(currentPos);

		Statement stmt = null;

		switch(currentToken.kind) {

		// { Statement* }
		case Token.LCURLY:
			acceptIt();
			StatementList statementList = new StatementList();
			while(currentToken.kind != Token.RCURLY) {
				statementList.add(parseStatement());
			}
			accept(Token.RCURLY);
			finish(currentPos);
			stmt = new BlockStmt(statementList, currentPos);
			break;

		// if  ( Expression )  Statement  (else  Statement)?
		case Token.IF:
			acceptIt();
			accept(Token.LPAREN);
			Expression expression = parseExpression();
			accept(Token.RPAREN);
			Statement thenStatement = parseStatement();
			if(currentToken.kind == Token.ELSE) {
				acceptIt();
				Statement elseStatement = parseStatement();
				finish(currentPos);
				stmt = new IfStmt(expression, thenStatement, elseStatement, currentPos);
			} else {
				finish(currentPos);
				stmt = new IfStmt(expression, thenStatement, currentPos);
			}
			break;

		// while  ( Expression )  Statement
		case Token.WHILE:
			acceptIt();
			//while(something happens) {statement}
			accept(Token.LPAREN);
			Expression whileExpression = parseExpression();
			accept(Token.RPAREN);
			Statement bodyStmt = parseStatement();
			finish(currentPos);
			stmt = new WhileStmt(whileExpression, bodyStmt, currentPos);
			break;

		case Token.RETURN:
			acceptIt();
			if(currentToken.kind == Token.SEMICOLON) {
				acceptIt();
				finish(currentPos);
				stmt = new ReturnStmt(null, currentPos);
				break;
			} else {
				//return  Expression? ;
				Expression returnExpr = parseExpression();
				accept(Token.SEMICOLON);
				finish(currentPos);
				stmt = new ReturnStmt(returnExpr, currentPos);
				break;
			}

		//this.foo = f00
		//this.foo(argument)
		case Token.THIS:
			SourcePosition thisPos = currentToken.position;
			acceptIt();

			//there will definitely be a dot afterwards
			Reference thisReference = parseMultipleRef(new ThisRef(thisPos));

			//Reference = Expression
			if(currentToken.kind == Token.EQUALS) {
				acceptIt();
				Expression assignExpression = parseExpression();
				accept(Token.SEMICOLON);
				finish(currentPos);
				stmt = new AssignStmt(thisReference, assignExpression, currentPos);
				return stmt;

			//  Reference  ( ArgumentList? ) ;
			} else if(currentToken.kind == Token.LPAREN) {
				acceptIt();
				ExprList arglist = new ExprList();
				if(currentToken.kind != Token.RPAREN) {
					arglist = parseArguments();
				}
				accept(Token.RPAREN);
				accept(Token.SEMICOLON);
				finish(currentPos);
				stmt = new CallStmt(thisReference, arglist, currentPos);
				return stmt;

			} else if(currentToken.kind == Token.LBRACKET) {
				//| Reference[Expression] = Expression
				acceptIt();
				Expression arrayExpression = parseExpression();
				accept(Token.RBRACKET);
				accept(Token.EQUALS);
				Expression equaledExpression = parseExpression();
				accept(Token.SEMICOLON);
				finish(currentPos);
				stmt = new IxAssignStmt(thisReference, arrayExpression, equaledExpression, currentPos);
				return stmt;
			}
			break;

		case Token.IDENTIFIER:
			//|  Reference = Expression
			//| Reference[Expression] = Expression CHECK
			//|  Reference  ( ArgumentList? ) ;

			Identifier firstId = new Identifier(currentToken);
			accept(Token.IDENTIFIER);

			boolean hasType = false;
			VarDecl varDecl = null;
			Reference reference = null;
			Identifier secondId = null;

			SourcePosition variablePos = new SourcePosition();

			//this is the name of an object of a class
			if(currentToken.kind == Token.IDENTIFIER) {
				hasType = true;
				secondId = new Identifier(currentToken);
				acceptIt();

				variablePos.start = firstId.posn.start;
				variablePos.finish = secondId.posn.finish;
				varDecl = new VarDecl(new ClassType(firstId, firstId.posn), secondId.spelling, secondId, variablePos);
			}
			//indicates that id.id.id.id etc.
			//reference is not null
			else if(currentToken.kind == Token.PERIOD) {
				SourcePosition identifierPos = new SourcePosition();
				identifierPos.start = currentPos.start;
				finish(identifierPos);
				reference = parseMultipleRef(new IdRef(firstId, identifierPos));

				if(currentToken.kind == Token.LBRACKET) {
					acceptIt();
					System.out.println("Reached");
					Expression arrayExpression;
					if(currentToken.kind == Token.RBRACKET) {
						acceptIt();
						arrayExpression = parseExpression();
						//Test [ ]  v = a;
					} else {
						arrayExpression = parseExpression();
						accept(Token.RBRACKET);
					}
					//accept(Token.RBRACKET);
					accept(Token.EQUALS);
					Expression equaledExpression = parseExpression();
					accept(Token.SEMICOLON);
					finish(currentPos);
					//check back on this part, not sure if correct reference or not
					stmt = new IxAssignStmt(reference, arrayExpression, equaledExpression, currentPos);
					return stmt;
				}
			}
			//indicates reference[expression]
			else if(currentToken.kind == Token.LBRACKET) {
				acceptIt();
				Expression arrayExpression;
				if(currentToken.kind == Token.RBRACKET) {
					acceptIt();
					arrayExpression = parseExpression();
					//Test [ ]  v = a;
				} else {
					arrayExpression = parseExpression();
					accept(Token.RBRACKET);
				}
				//accept(Token.RBRACKET);
				accept(Token.EQUALS);
				Expression equaledExpression = parseExpression();
				accept(Token.SEMICOLON);
				finish(currentPos);
				//check back on this part, not sure if correct reference or not
				stmt = new IxAssignStmt(new IdRef(firstId, firstId.posn), arrayExpression, equaledExpression, currentPos);
				return stmt;
			} else {
				//reference is not null
				reference = new IdRef(firstId, firstId.posn);
			}

			//   Type id  =  Expression ;
			if(currentToken.kind == Token.EQUALS) {
				acceptIt();
				Expression exp = parseExpression();
				accept(Token.SEMICOLON);
				finish(variablePos);
				if(reference == null) {
					//Type id = Expression
					stmt = new VarDeclStmt(varDecl, exp, currentPos);
				} else {
					//Reference = Expression
					stmt = new AssignStmt(reference, exp, currentPos);
				}
			} else if(currentToken.kind == Token.LPAREN && !hasType) {
				//  Reference  ( ArgumentList? ) ;
				acceptIt();
				ExprList arguments = new ExprList();
				if(currentToken.kind != Token.RPAREN) {
					arguments = parseArguments();
				}
				accept(Token.RPAREN);
				accept(Token.SEMICOLON);
				finish(currentPos);
				stmt = new CallStmt(reference, arguments, currentPos);
			}
			break;

		//Type  ::=  int  |  boolean  |  id  |  ( int | id ) []
			//Type id  =  Expression ;
		case Token.BOOLEAN:
		case Token.INT:
			TypeDenoter typeDenoter = parseType();
			Identifier id = new Identifier(currentToken);
			accept(Token.IDENTIFIER);
			accept(Token.EQUALS);
			Expression varDeclExp = parseExpression();
			accept(Token.SEMICOLON);
			finish(currentPos);
			stmt = new VarDeclStmt(new VarDecl(typeDenoter, id.spelling, id, id.posn), varDeclExp, currentPos);
			break;

		default:
			syntaxError("proper statement", Token.getKind(currentToken.kind), position_token);
		}

		return stmt;
	}

	private Reference parseMultipleRef(BaseRef baseRef) {
		SourcePosition pos = new SourcePosition();
		pos.start = baseRef.posn.start;
		Reference reference = baseRef;
		while(currentToken.kind == Token.PERIOD) {
			acceptIt();
			Identifier id = new Identifier(currentToken);
			accept(Token.IDENTIFIER);
			finish(pos);
			reference = new QualRef(reference, id, pos);
		}
		return reference;
	}

// ----------------------------------------------------------------------
//Expressions block of code
//Order of precedence means call disjunction first, then conjunction, then equality,
//then relational, then additive, then multiplicative, then unary
//	Reference
//	|  Reference  ( ArgumentList? )
//	|  unop  Expression
//	|  Expression  binop  Expression
//	|  ( Expression )
//	|  num  |  true  |  false
//	|  new  ( id ()  |  int [ Expression ]  |  id [ Expression ] )
//	| Reference [Expression]


	public void parseNum() {
		if (currentToken.kind == Token.NUM) {
			position_token = currentToken.position;
			currentToken = analyzer.scan();
		} else {
			syntaxError(Token.getKind(Token.NUM), Token.getKind(currentToken.kind), position_token);
		}
	}

	//begins the expression parsing
	public Expression parseExpression() {
		return parseDisjunctionExpression();
	}

	//Expression binop Expression
	//this is all done for the order of precedence for operators
	//parse disjunction first
	public Expression parseDisjunctionExpression() {
		Expression expression;
		SourcePosition pos = new SourcePosition();
		start(pos);
		//System.out.println("Reached disjuntion");
		expression = parseConjunctionExpression();
		while(currentToken.kind == Token.OR) {
			Operator operator = new Operator(currentToken);
			acceptIt();
			Expression newExpression = parseConjunctionExpression();
			finish(pos);
			expression = new BinaryExpr(operator, expression, newExpression, pos);
		}
		return expression;
	}

	//parse conjunction next
	public Expression parseConjunctionExpression() {
		Expression expression;
		SourcePosition pos = new SourcePosition();
		start(pos);
		//System.out.println("Reached conjunction");
		expression = parseEqualExpression();
		while(currentToken.kind == Token.AND) {
			Operator operator = new Operator(currentToken);
			acceptIt();
			Expression newExpression = parseEqualExpression();
			finish(pos);
			expression = new BinaryExpr(operator, expression, newExpression, pos);
		}
		return expression;
	}

	public Expression parseEqualExpression() {
		Expression expression;
		SourcePosition pos = new SourcePosition();
		start(pos);
		//System.out.println("Reached equality");
		expression = parseRelationalExpression();
		while(currentToken.kind == Token.EQUALITY || currentToken.kind == Token.NOT_EQUAL) {
			Operator operator = new Operator(currentToken);
			acceptIt();
			Expression newExpression = parseRelationalExpression();
			finish(pos);
			expression = new BinaryExpr(operator, expression, newExpression, pos);
		}
		return expression;
	}

	public Expression parseRelationalExpression() {
		Expression expression;
		SourcePosition pos = new SourcePosition();
		start(pos);
		//System.out.println("Reached relational");
		expression = parseAdditiveExpression();
		while(currentToken.kind == Token.GREATER || currentToken.kind == Token.GREATER_EQUAL ||
				currentToken.kind == Token.LESS || currentToken.kind == Token.LESS_EQUAL) {
			Operator operator = new Operator(currentToken);
			acceptIt();
			Expression newExpression = parseAdditiveExpression();
			finish(pos);
			expression = new BinaryExpr(operator, expression, newExpression, pos);
		}
		return expression;
	}

	public Expression parseAdditiveExpression() {
		Expression expression;
		SourcePosition pos = new SourcePosition();
		start(pos);
		//System.out.println("Reached add minus");
		expression = parseMultiplyExpression();
		while(currentToken.kind == Token.PLUS || currentToken.kind == Token.MINUS) {
			Operator operator = new Operator(currentToken);
			acceptIt();

			if(currentToken.kind == Token.NEGATION) {
				syntaxError("variable or expression", "Negation", pos);
			}

			Expression newExpression = parseMultiplyExpression();
			finish(pos);
			expression = new BinaryExpr(operator, expression, newExpression, pos);
		}
		return expression;
	}

	public Expression parseMultiplyExpression() {
		Expression expression;
		SourcePosition pos = new SourcePosition();
		start(pos);
		//System.out.println("Reached multiply divide");
		expression = parseUnaryExpression();
		while(currentToken.kind == Token.MULTIPLY || currentToken.kind == Token.DIVIDE) {
			Operator operator = new Operator(currentToken);
			acceptIt();
			Expression newExpression = parseUnaryExpression();
			finish(pos);
			expression = new BinaryExpr(operator, expression, newExpression, pos);
		}
		return expression;
	}

	//we have reached the end here, recursive calls from here on forward
	public Expression parseUnaryExpression() {
		Expression expression;
		SourcePosition pos = new SourcePosition();
		start(pos);
		//System.out.println("Reached unary");
		if(currentToken.kind == Token.MINUS || currentToken.kind == Token.NEGATION) {
			Operator operator = new Operator(currentToken);
			acceptIt();
			Expression newExpression = parseUnaryExpression();
			finish(pos);
			expression = new UnaryExpr(operator, newExpression, pos);
		} else {
			//System.out.println(currentToken.spelling);
			expression = parseActualExpression();
		}

		return expression;
	}

	public Expression parseActualExpression() {
		Expression expression = null;
		SourcePosition pos = new SourcePosition();

		start(pos);
		//System.out.println("Reached actual");
		switch(currentToken.kind) {
		//  num  |  true  |  false
		case Token.TRUE: case Token.FALSE:
			//System.out.println(currentToken.spelling);
			BooleanLiteral just_pay_pay_pay = new BooleanLiteral(currentToken);
			acceptIt();
			finish(pos);
			expression = new LiteralExpr(just_pay_pay_pay, pos);
			break;

		case Token.NUM:
			IntLiteral intLit = new IntLiteral(currentToken);
			acceptIt();
			finish(pos);
			expression = new LiteralExpr(intLit, pos);
			break;

		// new  ( id ()  |  int [ Expression ]  |  id [ Expression ] )
		case Token.NEW:
			acceptIt();

			if(currentToken.kind == Token.INT) {
				SourcePosition intPos = currentToken.position;
				acceptIt();
				accept(Token.LBRACKET);
				Expression indexExpression = parseExpression();
				accept(Token.RBRACKET);
				finish(pos);
				expression = new NewArrayExpr(new BaseType(TypeKind.INT, intPos), indexExpression, pos);
			} else if(currentToken.kind == Token.IDENTIFIER) {
				Identifier identifier = new Identifier(currentToken);
				acceptIt();
				if(currentToken.kind != Token.LPAREN) {
					accept(Token.LBRACKET);
					Expression indexExpression = parseExpression();
					accept(Token.RBRACKET);
					finish(pos);
					expression = new NewArrayExpr(new ClassType(identifier, identifier.posn), indexExpression, pos);
				} else {
					accept(Token.LPAREN);
					accept(Token.RPAREN);
					finish(pos);
					expression = new NewObjectExpr(new ClassType(identifier, identifier.posn), pos);
				}
			}
			break;

		// unop Expression
		//realized not needed due to being last order of precedence
		/*
		 * case Token.MINUS: case Token.NEGATION: Operator operator = new
		 * Operator(currentToken); acceptIt(); Expression unopExpression =
		 * parseExpression(); finish(pos); expression = new UnaryExpr(operator,
		 * unopExpression, pos);
		 */


		// ( Expression )
		case Token.LPAREN:
			acceptIt();
			expression = parseExpression();
			accept(Token.RPAREN);
			break;

		case Token.NULL:
			NullLiteral nullLiteral = new NullLiteral(currentToken);
			acceptIt();
			finish(pos);
			expression = new LiteralExpr(nullLiteral, pos);
			break;

		default:
		// Reference  ::=  id  |  this  |  Reference . id
			Reference reference = parseRef();

			//Reference  ( ArgumentList? )
			if(currentToken.kind == Token.LPAREN) {
				acceptIt();
				ExprList argumentList = new ExprList();
				if(currentToken.kind != Token.RPAREN) {
					argumentList = parseArguments();
				}
				accept(Token.RPAREN);
				finish(pos);
				expression = new CallExpr(reference, argumentList, pos);
				break;
			}
			//Reference[Expression]
			else if(currentToken.kind == Token.LBRACKET) {
				acceptIt();
				Expression indexExpression;
				if(currentToken.kind == Token.RBRACKET) {
					acceptIt();
					indexExpression = null;
				} else {
					indexExpression = parseExpression();
					accept(Token.RBRACKET);
				}

				finish(pos);
				expression = new IxExpr(reference, indexExpression, pos);
				break;
			}

			//Expression ::= Reference
			else {
				finish(pos);
				expression = new RefExpr(reference, pos);
				break;
			}
		}
		return expression;
	}

	//need to determine if the reference is just one reference or multiple (qualRef)
	public Reference parseRef() {
		BaseRef basedRef = parseOneRef();
		return parseMultipleRef(basedRef);
	}

	public BaseRef parseOneRef() {
		SourcePosition pos = new SourcePosition();
		start(pos);
		BaseRef baseRef = null;

		switch(currentToken.kind) {
		case Token.THIS:
			acceptIt();
			finish(pos);
			baseRef = new ThisRef(pos);
			break;

		case Token.IDENTIFIER:
			Identifier identifier = new Identifier(currentToken);
			acceptIt();
			//reference
			finish(pos);
			baseRef = new IdRef(identifier, pos);
			if(currentToken.kind == Token.NEGATION) {
				syntaxError("variable", "negation", pos);
			}
			break;
		/*
		 * if(currentToken.kind != Token.LBRACKET) { finish(pos); baseRef = new
		 * IdRef(identifier, pos); if(currentToken.kind == Token.NEGATION) {
		 * syntaxError("variable", "negation", pos); } break; }
		 */
			// Reference [Expression]
			/*
			 * else { acceptIt(); accept(Token.RBRACKET); finish(pos); baseRef = new
			 * IdRef(identifier, pos); break; }
			 */
		default:
			parseError("Attempted to parse one Reference");
		}

		return baseRef;
	}

}
