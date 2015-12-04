package ali_assembler.assembler;

public class Token {
	
	private Type t;
	private String value;

	public enum Type {
		opcode, param, label, newline, set, invalid;
	}
	
	public Token(Type t, String value) {
		this.t = t;
		this.value = value.toLowerCase().trim();
	}

	public Type getT() {
		return t;
	}

	public int getRegister() {
		String temp = value.replace("r", "");
		int toReturn = Integer.parseInt(temp);
		return toReturn & 15;
	}
	
	public String getString() {
		return value;
	}
	
	public int getNumber() {
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			return Integer.parseInt(value.substring(2), 16);
		}
	}
	
	public boolean isRegister() {
		return value.contains("r");
	}
	
	public boolean isNewLine() {
		return t.equals(Type.newline);
	}
	
	public boolean isSetFlag() {
		return t.equals(Type.set);
	}
	
	public boolean isInvalid() {
		return t.equals(Type.invalid);
	}
	
}
