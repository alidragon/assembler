package ali_assembler.assembler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MachineCode {
	
	private byte[] bytes;
	private int currentLoc;
	
	public MachineCode(int size) {
		bytes = new byte[size];
	}

	public void addByte(byte b) {
		bytes[currentLoc] = b;
		currentLoc++;
	}
	
	public void serialize() {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("kernel7.img");
			fos.write(bytes);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
