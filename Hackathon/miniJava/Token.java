import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Token {

	public int kind;
	public String spelling;
	public SourcePosition position;

	public Token(int kind, String spelling, SourcePosition position) {
		this.kind = kind;
		this.spelling = spelling;
		this.position = position;

		if (kind == Token.IDENTIFIER) {
			for(int i=5; i <= 19; i++) {
				if(spelling.equals(tokenIndex[i])) {
					this.kind = i;
					break;
				}
			}

			if(spelling.equals("null")) {
				this.kind = 33;
			}
		}
	}

public static Map<String, Integer> keywords; static {

		keywords = new HashMap<String, Integer>();
		keywords.put("class", Token.CLASS);
		keywords.put("static", Token.STATIC);
		keywords.put("private", Token.PRIVATE);
		keywords.put("public", Token.PUBLIC);
		keywords.put("void", Token.VOID);

		keywords.put("int", Token.INT);
		keywords.put("bool", Token.BOOLEAN);

		keywords.put("this", Token.THIS);
		keywords.put("return", Token.RETURN);
		keywords.put("else", Token.ELSE);
		keywords.put("if", Token.IF);
		keywords.put("while", Token.WHILE);

		keywords.put("false", Token.FALSE);
		keywords.put("true", Token.TRUE);

		keywords.put("new", Token.NEW);
		keywords.put("null", Token.NULL);
		keywords = Collections.unmodifiableMap(keywords);
	}

	private int findID(String spelling) {
		//Iterate through all the tokens and if we find that token
		//5 is index of first reserved word "new"
		//19 is index of last reserved word "while"
		for(int i=5; i <= 19; i++) {
			if(spelling.equals(tokenIndex[i])) {
				return i;
			}
		}
		return 5;
	}

	public static String getKind(int kind) {
		//System.out.println(kind);
	    return tokenIndex[kind];
	}

	public static boolean isBinaryOperator(String op) {
		for(int i=0; i < binaryOperators.length; i++) {
			if(binaryOperators[i].equals(op)) {
				return true;
			}
		}

		return false;
	}

	public static final int

		IDENTIFIER = 0,
		OPERATOR = 1,
		NUM = 2,
		BINOP = 3,
		UNOP = 4,

		NEW = 5,
		CLASS = 6,
		RETURN = 7,
		PRIVATE = 8,
		PUBLIC = 9,
		STATIC = 10,
		INT = 11,
		BOOLEAN = 12,
		VOID = 13,
		THIS = 14,
		TRUE = 15,
		FALSE = 16,
		IF = 17,
		ELSE = 18,
		WHILE = 19,

		EQUALS = 20,
		PERIOD = 21,
		COLON = 22,
		SEMICOLON = 23,
		COMMA = 24,

		LPAREN = 25,
		RPAREN = 26,
		LBRACKET = 27,
		RBRACKET = 28,
		LCURLY = 29,
		RCURLY = 30,
		EOT = 31,
		ERROR = 32,
		NULL = 33,

		//OPERATORS
		OR = 34,
		AND = 35,
		GREATER = 36,
		GREATER_EQUAL = 37,
		LESS = 38,
		LESS_EQUAL = 39,
		EQUALITY = 40,
		NOT_EQUAL = 41,
		PLUS = 42,
		MINUS = 43,
		MULTIPLY = 44,
		DIVIDE = 45,
		NEGATIVE = 46,
		NEGATION = 47,

		COMMENTS = 48,
		QUOTATION = 49,
		STRING = 50;

	public static String[] tokenIndex = new String[] {
		"<id>",
		"<operator>",
		"<num>",
		"<binop>",
		"<unop>",
		"new", //Reserved words
		"class",
		"return",
		"private",
		"public",
		"static",
		"int",
		"boolean",
		"void",
		"this",
		"true",
		"false",
		"if",
		"else",
		"while",
		"=",
		".",
		":",
		";",
		",",
		"(",
		")",
		"[",
		"]",
		"{",
		"}",
		"",
		"<error>",
		"null",
		"||",
		"&&",
		"<",
		"<=",
		">",
		">=",
		"==",
		"!=",
		"+",
		"-",
		"*",
		"/",
		"-",
		"!",
		"<comments>",
		"\"",
		"String"

	};

	public final static String[] binaryOperators = new String[] {
		"+",
		"-",
		"*",
		"/",
		"&&",
		"||",
		"<",
		">",
		"<=",
		">=",
		"==",
		"!="
	};
}
