import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;

public class Compiler {

	//Delimiter used in CSV file
  private static final String COMMA_DELIMITER = ",";
  private static final String NEW_LINE_SEPARATOR = "\n";

  //CSV file header
  private static final String FILE_HEADER = "Name,Class/Field/Method,Corresponding Class,Method Type,Documentation,Public/Private,Static,Parameters";

	public static void main(String[] args) throws FileNotFoundException {

	  FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(args[0]);
		} catch(FileNotFoundException e) {
		  System.out.println("Input file " + args[0] + " not found");
			System.exit(3);
		}

		ErrorReporter reporter = new ErrorReporter();
		Scanner scanner = new Scanner(inputStream);
		Parser parser = new Parser(scanner, reporter);

		System.out.println("Syntactic analysis");
		Package ast = parser.parseProgram();

		System.out.println("Syntactic analysis complete: ");
		if(reporter.getErrors()) {
			System.out.println("Invalid miniJava program");
			System.exit(4);
		} else {
			System.out.println("Valid miniJava program");
		}

		String fileName = "../Output_files/" + ast.classDeclList.get(0).name + ".csv";
		FileWriter fileWriter = null;

		try {
      	fileWriter = new FileWriter(fileName);

        //Write the CSV file header
        fileWriter.append(FILE_HEADER.toString());

        //Add a new line separator after the header
        fileWriter.append(NEW_LINE_SEPARATOR);

				//Name,Class/Field/Method,Corresponding Class,Method Type,Documentation,Public/Private,Static,Parameters
				for(ClassDecl c : ast.classDeclList) { //iterates through all the classes
					fileWriter.append(String.valueOf(c.name));
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("Class");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("---");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("---");
					fileWriter.append(COMMA_DELIMITER);
          String cdocsString = c.docsString.replace("\n", "").replace("\r", "").replace(",","$");
          System.out.println(cdocsString);
					fileWriter.append(cdocsString);
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("---");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("---");
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append("---");
					fileWriter.append(NEW_LINE_SEPARATOR);

          for(FieldDecl f : c.fieldDeclList) { //iterate through all fields of the class
            fileWriter.append(String.valueOf(f.name));
  					fileWriter.append(COMMA_DELIMITER);
  					fileWriter.append("Field");
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(c.name));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(f.type.typeKind));
            fileWriter.append(COMMA_DELIMITER);
            String fdocsString = f.docsString.replace("\n", "").replace("\r", "").replace(",","$");
            fileWriter.append(String.valueOf(fdocsString));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(f.isPrivate));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append(String.valueOf(f.isStatic));
            fileWriter.append(COMMA_DELIMITER);
            fileWriter.append("---");
            fileWriter.append(NEW_LINE_SEPARATOR);
          }

					for(MethodDecl m : c.methodDeclList) { //iterate through method list of classes
						fileWriter.append(String.valueOf(m.name));
						fileWriter.append(COMMA_DELIMITER);
						fileWriter.append("Method");
						fileWriter.append(COMMA_DELIMITER);
						fileWriter.append(String.valueOf(c.name));
						fileWriter.append(COMMA_DELIMITER);
						fileWriter.append(String.valueOf(m.type.typeKind));
						fileWriter.append(COMMA_DELIMITER);
            String mdocsString = m.docsString.replace("\n", "").replace("\r", "").replace(",","$");
						fileWriter.append(String.valueOf(mdocsString));
						fileWriter.append(COMMA_DELIMITER);
						fileWriter.append(String.valueOf(m.isPrivate));
						fileWriter.append(COMMA_DELIMITER);
						fileWriter.append(String.valueOf(m.isStatic));
						fileWriter.append(COMMA_DELIMITER);

						StringBuilder str = new StringBuilder();
						for(ParameterDecl p : m.parameterDeclList) { //iterate through parameters
							str.append(p.name);
							str.append(" (");
							str.append(p.type.typeKind);
							str.append(") ");
						}

						fileWriter.append(str.toString());
						fileWriter.append(NEW_LINE_SEPARATOR);
					}
				}

    		System.out.println("CSV file created successfully");

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter");
            e.printStackTrace();
        } finally {

            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter");
                e.printStackTrace();
            }

        }

		System.exit(0);
	}
}
