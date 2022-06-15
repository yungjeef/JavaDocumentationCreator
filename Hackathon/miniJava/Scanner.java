import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Scanner{

	//specific chars
	static final char EOL = '\n';
	static final char EOT = '\u0000';

	private java.io.File sourceFile;
	private java.io.FileInputStream inputStream;

	private char currentChar;
	private StringBuilder currentSpelling;
	private StringBuilder documentString;
	private int currentLine;
	private boolean currentlyScanning;

	public Scanner(FileInputStream fileName) {
		//inputStream = new FileInputStream(args[0]);
		this.inputStream = fileName;
		currentLine = 1;
		currentChar = getSource();

	}

	//get the line
	public char getSource() {
		try {
			int c = inputStream.read();
			//System.out.println((char) c);

			if(c == -1) {
				c = EOT;
			} else if(c == EOL) {
				currentLine++;
			}
			return (char) c;
		} catch(java.io.IOException s) {
			return EOT;
		}
	}

	public int getCurrentLine() {
		return currentLine;
	}

	public Token scanString() {
		Token toke;
		SourcePosition position;
		int tokenKind;
		currentSpelling = new StringBuilder("");

		position = new SourcePosition();
		position.start = getCurrentLine();
		tokenKind = scanStringToken();

		position.finish = getCurrentLine();
		toke = new Token(tokenKind, currentSpelling.toString(), position);

		return toke;
	}

	//skip whitespace and scan next separator
	public Token scan() {

		Token toke;
		SourcePosition position;
		int tokenKind;
		int scanSeparatorStatus = 1;

		//do not want comments and white space to be included in the current spelling
		currentlyScanning = false;

		while(currentChar == '\n' ||
				currentChar == ' ' ||
				currentChar == '\t' ||
				currentChar == '\r' ||
				currentChar == '/' ) {
			scanSeparatorStatus = scanSeparator();

			if(scanSeparatorStatus == -1) {
				break;
			} else if(scanSeparatorStatus == 2) {
				System.out.println("Comments present");
			} else if(scanSeparatorStatus == 3) {
				continue;
			}
		}

		currentlyScanning = true;
		currentSpelling = new StringBuilder("");
		position = new SourcePosition();
		position.start = getCurrentLine();

		//System.out.println(scanSeparatorStatus);

		if(scanSeparatorStatus == -1) {
			tokenKind = Token.DIVIDE;
			currentSpelling.append('/');
		} else {
			tokenKind = scanToken();
		}

		position.finish = getCurrentLine();
		toke = new Token(tokenKind, currentSpelling.toString(), position);

		return toke;
	}

	private int scanSeparator() {
		switch(currentChar) {
			//handling comments
			case '/': {
				takeIt();
				switch(currentChar) {
				case '/': {
					do {
						takeIt();
					}
					while(currentChar != EOL && currentChar != EOT);
				}
				return 2;

				case '*': {
					takeIt();
					while(true) {
						if(currentChar == EOT) {
							System.exit(4);
							return 0;
						}

						if(currentChar != '*') {
							takeIt();
							continue;
						}
						takeIt();

						if(currentChar == '/') {
							takeIt();
							break;
						}
					}
				}
				return 2;

				//token has been scanned instead
				default:
					return -1;
				}
			}

			//clears up white space
			case ' ': case '\n': case '\r': case '\t':{
				do{
					takeIt();
				}
				while(currentChar == ' ' ||
						currentChar == '\n' ||
						currentChar == '\r' ||
						currentChar == '\t');
				return 3;
			}

			default:
				return 0;
		}
	}

	//determine if inputed char is a letter or number
	private boolean isLetter(char c) {
		if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
			return true;
		}
		return false;
	}

	private boolean isDigit(char c) {
		return (c >= '0') && (c <= '9');
	}

	public int scanStringToken() {
		while(currentChar != '\"') {
			takeIt();
		}
		return Token.STRING;
	}

	//identify which token it is
	public int scanToken() {

		if(isLetter(currentChar)) {
			//only _ can be used in variable names
			while(isLetter(currentChar) || currentChar == '_' || isDigit(currentChar)) {
				takeIt();
			}
			return Token.IDENTIFIER;
		}

		if(isDigit(currentChar)) {
			while(isDigit(currentChar)) {
				takeIt();
			}
			return Token.NUM;
		}

		// scan Token
		switch (currentChar) {
			case '|':
				takeIt();
				if(currentChar == '|') {
					takeIt();
					return Token.OR;
				} else {
					return Token.ERROR;
				}

			case '&':
				takeIt();
				if(currentChar == '&') {
					takeIt();
					return Token.AND;
				} else {
					return Token.ERROR;
				}
			case ';':
				takeIt();
				return Token.SEMICOLON;
			case ',':
				takeIt();
				return Token.COMMA;
			case ':':
				takeIt();
				return Token.COLON;
			case '.':
				takeIt();
				return Token.PERIOD;
			case '>':
				takeIt();
				if(currentChar == '=') {
					takeIt();
					return Token.GREATER_EQUAL;
				}
				return Token.GREATER;
			case '<':
				takeIt();
				if(currentChar == '=') {
					takeIt();
					return Token.LESS_EQUAL;
				}
				return Token.LESS;
			case '!':
				takeIt();
				if(currentChar == '=') {
					takeIt();
				}
				return Token.NEGATION;
			case '=':
				takeIt();
				if(currentChar == '=') {
					takeIt();
					return Token.EQUALITY;
				} else {
					return Token.EQUALS;
				}
			case '+':
				takeIt();
				return Token.PLUS;
			case '-':
				takeIt();
				return Token.MINUS;
			case '*':
				takeIt();
				return Token.MULTIPLY;
			case '/':
				takeIt();
				return Token.DIVIDE;
			case ')':
				takeIt();
				return Token.RPAREN;
			case '}':
				takeIt();
				return Token.RCURLY;
			case '{':
				takeIt();
				return Token.LCURLY;
			case '(':
				takeIt();
				return Token.LPAREN;
			case ']':
				takeIt();
				return Token.RBRACKET;
			case '[':
				takeIt();
				return Token.LBRACKET;
			case '\"':
			  takeIt();
				return Token.QUOTATION;
			case EOT:
				return Token.EOT;
		}
		return Token.ERROR;
	}

	public String documentGeneration() {
		documentString = new StringBuilder("");
		currentChar = getSource();
		while(true) {
			currentChar = getSource();
			if(currentChar == '\"') {
				break;
			}
			documentString.append(currentChar);
		}
		return documentString.toString();
	}

	//take the currentChar and get a new value for currentChar
	private void takeIt() {
		//only add to the current spelling if not white space or comment
		if(currentlyScanning) {
			currentSpelling.append(currentChar);
		}
		currentChar = getSource();
	}

	private void skipIt() {
		currentChar = getSource();
	}
}
