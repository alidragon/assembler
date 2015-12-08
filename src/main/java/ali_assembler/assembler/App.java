package ali_assembler.assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import javax.naming.OperationNotSupportedException;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		String file = "assemblyHi";

		// Line counting to determine size of file to make; found on
		// http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
		int size = 0;
		try (LineNumberReader lnr = new LineNumberReader(new FileReader(new File(file)))) {
			lnr.skip(Long.MAX_VALUE);
			size = lnr.getLineNumber() + 1; // Add 1 because line index starts at 0
			// Finally, the LineNumberReader object should be closed to prevent
			// resource leak
			lnr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		size = 100;
		try {
			Tokenizer t = new Tokenizer(file);
			Assembler a = new Assembler(t, size * 4);
			a.AssembleAndSave();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OperationNotSupportedException e) {
			e.printStackTrace();
		} catch (CompilationException e) {
			e.printStackTrace();
		}

	}
}
