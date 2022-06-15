import java.util.ArrayList;
import java.util.List;

public class ErrorReporter {
	int numErrors;
	List<String> errorList = new ArrayList<String>();

	public ErrorReporter() {
		numErrors = 0;
	}

	public void reportError(String message) {

		String particularError = message;

		errorList.add(particularError);
		numErrors++;
		System.exit(4);
	}

	public void getErrorReport() {

		if(numErrors == 0) {
			System.out.print("Code is error free!");
		} else {
			for(String s : errorList) {
				System.out.println(s);
			}
			System.out.println("Total errors: " + numErrors);
		}
	}

	public boolean getErrors() {
		if(numErrors > 0) {
			return true;
		}
		return false;
	}
}
