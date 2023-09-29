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
import java.util.Arrays;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;


public class Lexer implements ILexer {

	String input;
	int pos;
	int length = 1;
	int line = 1;
	int column = 1;
    int startPos;
    char[] source;
    int count = 1;
    Boolean comment = false;
	private enum States {
        START, HAVE_EQUALS, IN_NUMB, IN_IDENT, HAVE_ZERO,HAVE_DOT,IN_FLOAT,STRING_LIT,LEFT,RIGHT,EXC,MINUS,NUM_LIT, BOX, AND, OR, TIMES, HASH
    }

	public Lexer(String input) {
		this.input = input;
	}
 
	Kind isWord(char[] source2) {
        length = source.length;
        System.out.println("isword");
        System.out.print("Source: ");
        System.out.println(source);
        System.out.println("Pos: " + pos);
        System.out.println("Length: " + length);
        System.out.println("Source Length: " + source.length);
        System.out.println("StartPos: " + startPos);
        System.out.println("\n");

        // forms arraylist into string
        String str = "";
        for (Object x : source2) {
            str += x;
        }
        //length = source.length;
        return switch (str) {
            
            case "TRUE", "FALSE" -> Kind.BOOLEAN_LIT;
            case "BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", "LIGHT_GRAY", "MAGENTA", "ORANGE", "PINK", "RED", "WHITE", "YELLOW", "Z" -> Kind.CONST;
            case "if" -> Kind.RES_if;
            case "fi" -> Kind.RES_fi;
            case "write" -> Kind.RES_write;
            case "void" -> Kind.RES_void;
            case "int" -> Kind.RES_int;
            case "string" -> Kind.RES_string;
            case "boolean" -> Kind.RES_boolean;
            case "image" -> Kind.RES_image;
            case "red" -> Kind.RES_red;
            case "green" -> Kind.RES_green;
            case "blue" -> Kind.RES_blue;
            case "width" -> Kind.RES_width;
            case "height" -> Kind.RES_height;
            case "pixel" -> Kind.RES_pixel;
            case "do" -> Kind.RES_do;
            case "od" -> Kind.RES_od;
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
	
    Kind Bruh() throws LexicalException {
        count++;
        if (count == 1) {
            //pos = 4;
            length=3;
            source = new char[3];
            source[0] = 'a';
            source[1] = 'b';
            source[2] = 'c';
            return Kind.IDENT;
        }
        else if (count == 2) {
            //pos = 8;
            length=3;
            source = new char[3];
            source[0] = '1';
            source[1] = '2';
            source[2] = '3';
            return Kind.NUM_LIT;
        }
        else if (count == 3) {
            //pos = 14;
            length=6;
            source = new char[6];
            source[0] = 'a';
            source[1] = 'b';
            source[2] = 'c';
            source[3] = '1';
            source[4] = '2';
            source[5] = '3';
            return Kind.IDENT;
        }
        
        return EOF;
    }
    
	Kind L(String input) throws LexicalException {
        
		States state = States.START;
	    char[] chars = (input+'~').toCharArray();
        Kind k;
        source = new char[0];
        column=count;
        length = source.length;
		while(true) {
            
			char ch = chars[pos];
            
            if (ch != ' ' && ch != '\n') {
                if (state != States.IN_IDENT && state != States.NUM_LIT) {
                    if (ch != '~') {
                        source = Arrays.copyOf(source, source.length + 1);
                        source[source.length - 1] = ch;
                    }
                    
                }
                
            }
            
            
			switch (state) {
                
            
				case START -> {
                    System.out.println("Character: \"" + ch + "\"");
                    System.out.print("Source: ");
                    System.out.println(source);
                    System.out.println("Source Length: " + source.length);
                    System.out.println("Pos: " + pos);
                    System.out.println("StartPos: " + startPos);
                    System.out.println("Length: " + length);
                    System.out.println("State: " + state);
                    System.out.println("Count: " + count);
                    System.out.println("Column: " + column);
                    System.out.println("\n");
                    
                    

                    if (pos < input.length() && (getASCII(input)[pos] == 10)) {
                        ch = '\n';
                    }
                    switch (ch) {
                        case ' ','\t','\r' -> {pos++;count++;column=count;}
                        case '\n'-> {line++;pos++;count=1;column=count;}
                        case '+' -> {length=1;pos++;return Kind.PLUS;}
                        case '#' -> {pos++;state = States.HASH;}
                        case 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
                        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                                '$','_' -> {pos++;state = States.IN_IDENT;}
                        case '\"' -> {state = States.STRING_LIT;pos++;length++;}
                        case '1','2','3','4','5','6','7','8','9' -> {state = States.NUM_LIT;pos++;}
                        case '(' -> {pos++;return Kind.LPAREN;}
                        case ')' -> {pos++;return Kind.RPAREN;}
                        case '[' -> {pos++;state = States.BOX;}
                        case ']' -> {pos++;return Kind.RSQUARE;}
                        case '<' -> {pos++;state = States.LEFT;}
                        case '>' -> {pos++;state = States.RIGHT;}
                        case '/' -> {pos++;return Kind.DIV;}
                        case '*' -> {pos++;state = States.TIMES;}
                        case '-' -> {state = States.MINUS;pos++;}
                        case '=' -> {state = States.HAVE_EQUALS; pos++;}
                        case '0' -> {state = States.HAVE_ZERO;pos++;}
                        case '%' -> {pos++;return Kind.MOD;}
                        case '&' -> {state = States.AND;pos++;}
                        case '|' -> {pos++;state = States.OR;}
                        case '!' -> {pos++;return Kind.BANG;}
                        case ';' -> {pos++;return Kind.SEMI;}
                        case ',' -> {pos++;return Kind.COMMA;}
                        case '^' -> {pos++;return Kind.RETURN;}
                        case '?' -> {pos++;return Kind.QUESTION;}
                        case '~' -> {return Kind.EOF;}
                        default -> {System.out.println("Throwing Lexical Exception.");throw new LexicalException("invalid lexer bug");}
                    }
                    }
                case HASH -> {
                    switch(ch) {
                        case'#' ->{
                            comment = true;
                            pos++;
                        }
                        default -> {
                            if (comment) {
                                pos++;
                                if(ch == '\n') {
                                    System.out.println(source);
                                    comment = false;
                                    state = States.START;
                                    source = new char[0];
                                    
                                }
                                
                            }
                            else {
                                throw new LexicalException("hash lexer bug");
                            }
                        }
                            
                    }
                            
                }
                
                case TIMES -> {
                    switch(ch){
                                case'*'-> {
                                pos++;
                                length=source.length;
                                return Kind.EXP;
                            }
                            default -> {return Kind.TIMES;}
                            }
                }
                case OR -> {
                    switch(ch) {
                        case '|' -> {
                        pos++;
                        length=source.length;
                        return Kind.OR;
                        }
                    default -> {
                        return Kind.BITOR;
                    }
                    }
                }
                case AND -> {
                    switch (ch) {
                        case '&' -> {

                            System.out.print("Source: ");
                            System.out.println(source);
                            System.out.println("Source Length: " + source.length);
                            
                            System.out.println("Count: " + count);
                            System.out.println("col: " + column);
                            pos++;
                            length=source.length;
                            count+= length;
                            return Kind.AND;
                        }
                        default -> {
                            

                            System.out.print("Source: ");
                            System.out.println(source);
                            System.out.println("Source Length: " + source.length);
                            
                            System.out.println("Count: " + count);
                            System.out.println("col: " + column);
                            length=source.length;
                            count+= length;
                            return Kind.BITAND;
                        }
                    }
                }
                case BOX -> {
                    switch (ch) {
                        case ']' -> {
                            pos++;
                            return Kind.BOX;
                        }
                        default -> {
                            return Kind.LSQUARE;
                        }
                    }
                }

				case IN_IDENT-> {
                    
                    switch(ch) {
                        case 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
                                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                                '$','_','0','1','2','3','4','5','6','7','8','9' -> {
                            source = Arrays.copyOf(source, source.length + 1);
                            source[source.length - 1] = ch;
                            pos++;
                            length = source.length;
                        }
                        
                        default -> {
                            //source = Arrays.copyOf(source, source.length - 1);
                            length = source.length;

                            return isWord(source);
                        }
                    }
                }
				case HAVE_ZERO -> {
                    switch(ch) {
                        case '.' -> {
                            state = States.HAVE_DOT;
                            pos++;
                        }
                        default -> {
                            return Kind.NUM_LIT;
                        }
                    }
                }
                
				
				case HAVE_DOT -> {
                    switch(ch) {
                        case '0','1','2','3','4','5','6','7','8','9' -> {
                            state = States.IN_FLOAT;
                            pos++;
                        }
                        default -> throw new LexicalException("lexer bug");

                    }
                }
                case HAVE_EQUALS -> {
                    switch (ch) {
                        case '=' -> {
                            pos++;
                            length = source.length;
                            return Kind.EQ;
                        }
                        default -> {return Kind.ASSIGN;}
                        
                    }
                }

                case IN_NUMB -> {
                    switch(ch) {
                        case '0','1','2','3','4','5','6','7','8','9' -> {
                            pos++;
                        }
                        default -> {
                            length = source.length;
                            return Kind.NUM_LIT;
                        }
                    }
                }

				
                case NUM_LIT -> {
                    switch(ch) {
                        case '0','1','2','3','4','5','6','7','8','9' -> {
                            source = Arrays.copyOf(source, source.length + 1);
                            source[source.length - 1] = ch;
                            column=count-source.length;
                            if (source.length > 11) {
                                throw new LexicalException("num_lit bug");
                            }
                            
                            pos++;
                            length++;
                            
                            
                        }
                        default -> {
                        length = source.length;
                            return Kind.NUM_LIT;

                        }
                    }
                }
                case STRING_LIT -> {
                    
                    switch(ch) {
                        
                        case '"' -> {
                            
                            pos++;
                            length = source.length;
                            return Kind.STRING_LIT;
                        }
                        case '\n' -> {
                            throw new LexicalException("string_lit bug");
                        }
                        case '␡' -> {
                            throw new LexicalException("string_lit bug");
                        }
                        case '~' -> {
                            throw new LexicalException("string_lit bug");
                        }
                        default -> {
                            //source = Arrays.copyOf(source, source.length + 1);
                            //source[source.length - 1] = ch;
                            pos++;
                        }
                    }
                }
                case MINUS -> {
                    switch (ch) {
                        case '>' -> {
                            pos++;
                            return Kind.RARROW;
                        }
                        default -> {
                            //pos++;
                            return Kind.MINUS;
                        }
                    }
                }
                case LEFT -> {
                    switch (ch) {
                        case '=' -> {
                            pos++;
                            length++;
                            return Kind.LE;
                        }/*
                        case '-' -> {
                            pos++;
                            length++;
                            return Kind.LEFTARROW;
                        }*/
                        case ':' -> {
                            pos++;
                            length++;
                            return Kind.BLOCK_OPEN;
                        }
                        default -> {
                            return Kind.LT;
                        }
                    }

                }
                case RIGHT -> {
                    switch (ch) {
                        case '=' -> {
                            pos++;
                            length++;
                            return Kind.GE;
                        }
                        default -> {
                            //pos++;
                            return Kind.GT;
                        }
                    }
                }
                
                
                // if character is an identifier
                
                case IN_FLOAT -> {
					switch(ch) {
                        case '0','1','2','3','4','5','6','7','8','9' -> {
                            source = Arrays.copyOf(source, source.length + 1);
                            source[source.length - 1] = ch;
                            pos++;
                        }
                        default -> {
                            return Kind.NUM_LIT;
                        }
                    }
                }
				
				//default -> {return Kind.EOF;}
				default -> throw new LexicalException("invalid lexer bug");
			}
            
        }
		
	}
	@Override
	public IToken next() throws LexicalException {

		return new Token(L(input), startPos, length, source, new SourceLocation(line, column));
        //return new Token(Bruh(), startPos, length, source, new SourceLocation(line, column));
	}
}

