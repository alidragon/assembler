package ali_assembler.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import ali_assembler.assembler.Token.Type;

public class Assembler {
	private Tokenizer tokens;
	private MachineCode code;
	private int currentLine = 1;

	private static final byte E = 14;
	private static final byte B = 11;
	private static final byte A = 10;
	private static final byte HIGH_NIBBLE_MULTIPLIER = 16;

	private HashMap<String, Integer> labels;

	public Assembler(Tokenizer t, int size) {
		tokens = t;
		code = new MachineCode(size);
		labels = new HashMap<>();
	}

	public void AssembleAndSave() throws CompilationException, OperationNotSupportedException {
		currentLine = 1;
		int currentLineLabels = 1;
		List<Token> tokenList = new ArrayList<>();
		tokenList.add(new Token(Type.opcode, "movw"));
		tokenList.add(new Token(Type.param, "r13"));
		tokenList.add(new Token(Type.param, "0x8000"));
		currentLineLabels++;
		while(tokens.hasNext()) {
			Token t = tokens.next();
			tokenList.add(t);
			if(t.getT() == Token.Type.label) {
				labels.put(t.getString(), currentLineLabels);
			} else if(t.getT() == Token.Type.newline) {
				currentLine++;				
				currentLineLabels++;
			} else if(t.getT() == Type.opcode) {
				if(t.getString().equals("call")) {
					Token label = tokens.next();
					tokenList.add(label);
					//currentLineLabels++;
//					tokenList.add(new Token(Type.opcode, "strw"));
//					tokenList.add(new Token(Type.param, "r13"));
//					tokenList.add(new Token(Type.param, "r14"));
//					currentLineLabels++;
//					for(int i = 12; i >= 0; i--) {
//						tokenList.add(new Token(Type.opcode, "strw"));
//						tokenList.add(new Token(Type.param, "r13"));
//						tokenList.add(new Token(Type.param, "r" + i));
//						currentLineLabels++;
//					}
					
					//branch w/ link to other code
					tokenList.add(new Token(Type.opcode, "bl"));
					tokenList.add(new Token(Type.param, label.getString()));
					currentLineLabels++;
					
					//get link from stack, since you couldn't do that earlier
//					tokenList.add(new Token(Type.opcode, "ldrw"));
//					tokenList.add(new Token(Type.param, "r13"));
//					tokenList.add(new Token(Type.param, "r14"));
//					currentLineLabels++;
				} else if(t.getString().equals("return")) {
					//get registers 0-12 from the stack
//					for(int i = 0; i < 13; i++) {
//						tokenList.add(new Token(Type.opcode, "ldrw"));
//						tokenList.add(new Token(Type.param, "r13"));
//						tokenList.add(new Token(Type.param, "r" + i));
//						currentLineLabels++;
//					}
					//Put Link into PC
					tokenList.add(new Token(Type.opcode, "mov"));
					tokenList.add(new Token(Type.param, "r15"));
					tokenList.add(new Token(Type.param, "r14"));
					currentLineLabels++;
				}
			}
		}
		System.out.println("Labels: ");
		for(String s : labels.keySet()) {
			System.out.println(s + ": " + labels.get(s));
		}
		System.out.println("----------------");
		currentLine = 1;
		currentLineLabels = 1;
		Iterator<Token> tokenIt = tokenList.iterator();
		try {
			while(tokenIt.hasNext()) {
				Token t = tokenIt.next();
				if(t == null) {
					break;
				}
				switch(t.getT()) {
					case newline:
						currentLine++;
						break;
					case opcode:
						currentLineLabels++;
						switch(t.getString()) {
							case "movw": //movw	rd, imm
								int register = tokenIt.next().getRegister();
								int immediate = tokenIt.next().getNumber();
								
								String binaryImmediate = length(Integer.toBinaryString(immediate), 16);
								
								code.addByte(Byte.parseByte(binaryImmediate.substring(8), 2));
								code.addByte((byte) (register * (HIGH_NIBBLE_MULTIPLIER) + Byte.parseByte(binaryImmediate.substring(4, 8), 2)));
								code.addByte(Byte.parseByte(binaryImmediate.substring(0, 4), 2));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + 3));
								break;
							case "mov": //mov rd, rm    (move rm to rd)
								register = tokenIt.next().getRegister();
								int rm = tokenIt.next().getRegister();
								
								code.addByte((byte) rm);
								code.addByte((byte) (register * (HIGH_NIBBLE_MULTIPLIER)));
								code.addByte((byte) (A * HIGH_NIBBLE_MULTIPLIER));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + 1));
								break;
							case "add":
								Token token = tokenIt.next();
								boolean set = false;
								if(token.getT() == Token.Type.set) {
									set = token.isSetFlag();
									register = tokenIt.next().getRegister();
								} else {
									register = token.getRegister();
								}
								int rd = tokenIt.next().getRegister();
								
								token = tokenIt.next();
								boolean isImmediate = false;
								if(token.isRegister()) {
									//make logic for register stuff
									
									throw new OperationNotSupportedException();
									//code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER));
									
								} else {
									isImmediate = true;
									immediate = token.getNumber();
									binaryImmediate = length(Integer.toBinaryString(immediate), 8);
									
									int rotate = tokenIt.next().getNumber();

									code.addByte(Byte.parseByte(binaryImmediate, 2));
									code.addByte((byte) (rd * HIGH_NIBBLE_MULTIPLIER + rotate));
									
								}
								code.addByte((byte) (((set ? 1 : 0) + 8) * HIGH_NIBBLE_MULTIPLIER + register));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + (isImmediate? 2 : 0)));
								
								break;
							case "movt":
								register = tokenIt.next().getRegister();
								immediate = tokenIt.next().getNumber();
								
								binaryImmediate = length(Integer.toBinaryString(immediate), 16);
								
								code.addByte(Byte.parseByte(binaryImmediate.substring(8), 2));
								code.addByte((byte) (register * HIGH_NIBBLE_MULTIPLIER + Byte.parseByte(binaryImmediate.substring(4, 8), 2)));
								code.addByte((byte) (4 * HIGH_NIBBLE_MULTIPLIER + Byte.parseByte(binaryImmediate.substring(0, 4), 2)));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + 3));
								break;
							case "ldr":
								register = tokenIt.next().getRegister();
								rd = tokenIt.next().getRegister();
								
								code.addByte((byte)0);
								code.addByte((byte) (rd * HIGH_NIBBLE_MULTIPLIER));
								code.addByte((byte) (1 * HIGH_NIBBLE_MULTIPLIER + register));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + 4));
								break;
							case "str":
								register = tokenIt.next().getRegister();
								rd = tokenIt.next().getRegister();
								
								code.addByte((byte)0);
								code.addByte((byte) (rd * HIGH_NIBBLE_MULTIPLIER));
								code.addByte((byte) (register));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + 4));
								break;
							case "strw":
								register = tokenIt.next().getRegister();
								rd = tokenIt.next().getRegister();
								
								code.addByte((byte)0);
								code.addByte((byte) (rd * HIGH_NIBBLE_MULTIPLIER));
								code.addByte((byte) (2 * HIGH_NIBBLE_MULTIPLIER + register));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + 4));
								break;
							case "ldrw":
								register = tokenIt.next().getRegister();
								rd = tokenIt.next().getRegister();
								
								code.addByte((byte)0);
								code.addByte((byte) (rd * HIGH_NIBBLE_MULTIPLIER));
								code.addByte((byte) (B * HIGH_NIBBLE_MULTIPLIER + register));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + 5));
								break;
							case "orr":
								token = tokenIt.next();
								set = false;
								if(token.getT() == Token.Type.set) {
									set = token.isSetFlag();
									register = tokenIt.next().getRegister();
								} else {
									register = token.getRegister();
								}
								rd = tokenIt.next().getRegister();
								
								token = tokenIt.next();
								isImmediate = false;
								if(token.isRegister()) {
									//make logic for register stuff
									
									throw new OperationNotSupportedException();
									//code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER));
									
								} else {
									isImmediate = true;
									immediate = token.getNumber();
									binaryImmediate = length(Integer.toBinaryString(immediate), 8);
									
									int rotate = tokenIt.next().getNumber();

									code.addByte(Byte.parseByte(binaryImmediate, 2));
									code.addByte((byte) (rd * HIGH_NIBBLE_MULTIPLIER + rotate));
									
								}
								code.addByte((byte) (((set ? 1 : 0) + 8) * HIGH_NIBBLE_MULTIPLIER + register));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + (isImmediate? 2 : 0) + 1));
								
								break;
							case "bal":
								t = tokenIt.next();
								try {
									immediate = t.getNumber();
									
									binaryImmediate = length(Integer.toBinaryString(immediate), 24);
									
									code.addByte((byte) Integer.parseInt(binaryImmediate.substring(16, 24), 2));
									code.addByte((byte) Integer.parseInt(binaryImmediate.substring(8, 16), 2));
									code.addByte((byte) Integer.parseInt(binaryImmediate.substring(0, 8), 2));
								} catch(NumberFormatException e) {
									String label = t.getString();
									if(!labels.containsKey(label)) {
										throw new CompilationException("label does not exist");
									}
									int labelLoc = labels.get(label);
									int diff = labelLoc - currentLineLabels - 3;
						            code.addByte((byte) diff);
						            code.addByte((byte)(diff >>> 8));
									code.addByte((byte)(diff >>> 16));
								}
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + 10));
								break;
							case "bl":
								t = tokenIt.next();
								try {
									immediate = t.getNumber();
									
									binaryImmediate = length(Integer.toBinaryString(immediate), 24);
									
									code.addByte((byte) Integer.parseInt(binaryImmediate.substring(16, 24), 2));
									code.addByte((byte) Integer.parseInt(binaryImmediate.substring(8, 16), 2));
									code.addByte((byte) Integer.parseInt(binaryImmediate.substring(0, 8), 2));
								} catch(NumberFormatException e) {
									String label = t.getString();
									if(!labels.containsKey(label)) {
										throw new CompilationException("label does not exist");
									}
									int labelLoc = labels.get(label);
									int diff = labelLoc - currentLineLabels - 3;
									System.out.println("DIFF: " + diff);
						            code.addByte((byte) diff);
						            code.addByte((byte)(diff >>> 8));
									code.addByte((byte)(diff >>> 16));
								}
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + B));
								break;
							case "sub":
								token = tokenIt.next();
								set = false;
								if(token.getT() == Token.Type.set) {
									set = token.isSetFlag();
									register = tokenIt.next().getRegister();
								} else {
									register = token.getRegister();
								}
								rd = tokenIt.next().getRegister();
								
								token = tokenIt.next();
								isImmediate = false;
								if(token.isRegister()) {
									//make logic for register stuff
									
									throw new OperationNotSupportedException();
									//code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER));
									
								} else {
									isImmediate = true;
									immediate = token.getNumber();
									binaryImmediate = length(Integer.toBinaryString(immediate), 8);
									
									int rotate = tokenIt.next().getNumber();

									code.addByte(Byte.parseByte(binaryImmediate, 2));
									code.addByte((byte) (rd * HIGH_NIBBLE_MULTIPLIER + rotate));
									
								}
								code.addByte((byte) (((set ? 1 : 0) + 4) * HIGH_NIBBLE_MULTIPLIER + register));
								code.addByte((byte) (E * HIGH_NIBBLE_MULTIPLIER + (isImmediate? 2 : 0)));
								break;
							case "bne":
								t = tokenIt.next();
								try {
									immediate = t.getNumber();
									
									binaryImmediate = length(Integer.toBinaryString(immediate), 24);
									
									code.addByte((byte) Integer.parseInt(binaryImmediate.substring(16, 24), 2));
									code.addByte((byte) Integer.parseInt(binaryImmediate.substring(8, 16), 2));
									code.addByte((byte) Integer.parseInt(binaryImmediate.substring(0, 8), 2));
								} catch(NumberFormatException | StringIndexOutOfBoundsException e) {
									String label = t.getString();
									if(!labels.containsKey(label)) {
										throw new CompilationException("label does not exist");
									}
									int labelLoc = labels.get(label);
									int diff = labelLoc - currentLineLabels - 2;
						            code.addByte((byte) diff);
						            code.addByte((byte)(diff >>> 8));
									code.addByte((byte)(diff >>> 16));
								}
								code.addByte((byte) (1 * HIGH_NIBBLE_MULTIPLIER + 10));
								break;
							case "call":
								tokenIt.next();
								currentLineLabels--;
								break;
							case "return":
								currentLineLabels--;
								break;
							default:
								break;
						}
						break;
					case label:
						break;
					case param:
					case invalid:
					default:
						throw new CompilationException("Invalid instruction at line: " + currentLine);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw new CompilationException("Invalid instruction at line: " + currentLine);
		}

		for(int i = 0; i < tokenList.size(); i++) {
			System.out.println(tokenList.get(i).getString());
		}
		code.serialize();
	}

	private String length(String binaryString, int size) {
		while (binaryString.length() < size) {
			binaryString = "0" + binaryString;
		}
		return binaryString;
	}

}
