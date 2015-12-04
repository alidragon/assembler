package ali_assembler.assembler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;

import ali_assembler.assembler.Token.Type;

public class Tokenizer implements Iterator<Token> {
	
	private BufferedReader fileStream;
	private Deque<Token> tokenQueue;
	private String file;
	
	public Tokenizer(String file) throws FileNotFoundException {
		fileStream = new BufferedReader(new FileReader(file));
		this.file = file;
		tokenQueue = new ArrayDeque<>();
	}
	
	public void restart() {
		try {
			fileStream.close();
			fileStream = new BufferedReader(new FileReader(file));
			tokenQueue = new ArrayDeque<>();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public boolean hasNext() {
		try {
			return tokenQueue.size() > 0 || fileStream.ready();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void addNext(Token t) {
		tokenQueue.push(t);
	}

	public Token next() {
		if(tokenQueue.size() > 0) {
			return tokenQueue.poll();
		} 
		try {
			if(fileStream.ready()) {
				tokenize(fileStream.readLine());
				return tokenQueue.size() > 0 ? tokenQueue.poll() : null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void tokenize(String line) {
		try {
			line = line.split(";")[0];
			String[] parts = line.split("\t");
			if(parts[0].length() > 0) {
				tokenQueue.add(new Token(Token.Type.label, parts[0]));
			}
			if(parts[1].length() < 1) {
				tokenQueue.add(new Token(Token.Type.invalid, parts[1]));
			}
			if(parts[1].charAt(parts[1].length() - 1) == 's') {
				tokenQueue.add(new Token(Token.Type.opcode, parts[1].substring(0, parts[1].length() - 1)));
				tokenQueue.add(new Token(Token.Type.set, "s"));
			} else {
				tokenQueue.add(new Token(Token.Type.opcode, parts[1]));
			}
			if(parts.length == 3) {
				String[] options = parts[2].split(",");
				for(int i = 0; i < options.length; i++) {
					tokenQueue.add(new Token(Token.Type.param, options[i]));
				}
				tokenQueue.add(new Token(Token.Type.newline, "\n"));
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			tokenQueue.add(new Token(Token.Type.invalid, line));
		}
	}

}
