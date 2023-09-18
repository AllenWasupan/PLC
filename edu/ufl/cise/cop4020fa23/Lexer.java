/*Copyright 2023 by Beverly A Sanders
* 
* This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
* University of Florida during the fall semester 2023 as part of the course project.  
* 
* No other use is authorized. 
* 
* This code may not be posted on a public web site either during or after the course.  
*/
package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.EOF;

import java.util.ArrayList;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;


public class Lexer implements ILexer {

	String input;


	private enum States {
        START, HAVE_EQUALS, IN_NUMB, IN_IDENT, HAVE_ZERO,HAVE_DOT,IN_FLOAT,STRING_LIT,LEFT,RIGHT,EXC,MINUS,INT_LIT
    }

	public Lexer(String input) {
		this.input = input;
	}
/* 
	Kind isWord(ArrayList input) {
        // forms arraylist into string
        String str = "";
        for (Object x : input) {
            str += x;
        }

        // checks for different Kinds
        return switch (str) {
            case "true", "false" -> Kind.BOOLEAN_LIT;
            case "BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", "LIGHT_GRAY", "MAGENTA", "ORANGE", "PINK", "RED", "WHITE", "YELLOW" -> Kind.COLOR_CONST;
            case "if" -> Kind.KW_IF;
            case "fi" -> Kind.KW_FI;
            case "else" -> Kind.KW_ELSE;
            case "write" -> Kind.KW_WRITE;
            case "console" -> Kind.KW_CONSOLE;
            case "void" -> Kind.KW_VOID;
            case "int", "float", "string", "boolean", "color", "image" -> Kind.TYPE;
            case "getRed", "getGreen", "getBlue" -> Kind.COLOR_OP;
            case "getWidth", "getHeight" -> Kind.IMAGE_OP;
            default -> Kind.IDENT;
        };

    }

	int[] getASCII(String s) {
        int[] ascii = new int[s.length()];
        for (int i = 0; i != s.length(); i++) {
            ascii[i] = s.charAt(i);
        }
        return (ascii);
    }
	*/
	Kind L(String input) throws LexicalException {
		States state = States.START;
		int pos = 0;
		int length = 1;
		int line = 0;
		int column = 0;
        char[] chars = (input+'~').toCharArray();
		while(true) {
			char ch = chars[pos];
			switch (ch) {
				
				default -> throw new LexicalException("lexer bug");
			}
		}
		
	}
	@Override
	public IToken next() throws LexicalException {
		return new Token(L(input), 0, 0, null, new SourceLocation(1, 1));
	}




}
