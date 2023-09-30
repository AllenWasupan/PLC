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

import static edu.ufl.cise.cop4020fa23.Kind.AND;
import static edu.ufl.cise.cop4020fa23.Kind.BANG;
import static edu.ufl.cise.cop4020fa23.Kind.BITAND;
import static edu.ufl.cise.cop4020fa23.Kind.BITOR;
import static edu.ufl.cise.cop4020fa23.Kind.COLON;
import static edu.ufl.cise.cop4020fa23.Kind.COMMA;
import static edu.ufl.cise.cop4020fa23.Kind.DIV;
import static edu.ufl.cise.cop4020fa23.Kind.EOF;
import static edu.ufl.cise.cop4020fa23.Kind.EQ;
import static edu.ufl.cise.cop4020fa23.Kind.EXP;
import static edu.ufl.cise.cop4020fa23.Kind.GE;
import static edu.ufl.cise.cop4020fa23.Kind.GT;
import static edu.ufl.cise.cop4020fa23.Kind.IDENT;
import static edu.ufl.cise.cop4020fa23.Kind.LE;
import static edu.ufl.cise.cop4020fa23.Kind.LPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.LSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.LT;
import static edu.ufl.cise.cop4020fa23.Kind.MINUS;
import static edu.ufl.cise.cop4020fa23.Kind.MOD;
import static edu.ufl.cise.cop4020fa23.Kind.NUM_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.OR;
import static edu.ufl.cise.cop4020fa23.Kind.PLUS;
import static edu.ufl.cise.cop4020fa23.Kind.QUESTION;
import static edu.ufl.cise.cop4020fa23.Kind.RARROW;
import static edu.ufl.cise.cop4020fa23.Kind.RES_blue;
import static edu.ufl.cise.cop4020fa23.Kind.RES_green;
import static edu.ufl.cise.cop4020fa23.Kind.RES_height;
import static edu.ufl.cise.cop4020fa23.Kind.RES_red;
import static edu.ufl.cise.cop4020fa23.Kind.RES_width;
import static edu.ufl.cise.cop4020fa23.Kind.RPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.RSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.BOOLEAN_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.TIMES;
import static edu.ufl.cise.cop4020fa23.Kind.CONST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.ASTVisitor;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
/**
Expr::=  ConditionalExpr | LogicalOrExpr   
ConditionalExpr ::=  ?  Expr  ->  Expr  ,  Expr  
LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)* 
LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)* 
ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)* 
PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr 
AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )* 
MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)* 
UnaryExpr ::=  ( ! | - | width | height) UnaryExpr  |  PostfixExpr 
PostfixExpr::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε ) 
PrimaryExpr ::=STRING_LIT | NUM_LIT |  IDENT | ( Expr ) | CONST | 
    ExpandedPixelExpr 
ChannelSelector ::= : red | : green | : blue 
PixelSelector  ::= [ Expr , Expr ] 
ExpandedPixelExpr ::= [ Expr , Expr , Expr ]
 */

public class ExpressionParser implements IParser {
	
	final ILexer lexer;
	private IToken t;
	
	ExpressionParser(String input) throws SyntaxException {
        lexer = ComponentFactory.makeLexer(input);
        
    }
	protected boolean isKind(Kind kind) {
        return t.kind() == kind;
		
	}
	
	protected boolean isKind(Kind... kinds) throws SyntaxException {
		for (Kind k : kinds) {
		   if (k == t.kind())
		   return true;
		}
		return false;
	}

	/**
	 * @param lexer
	 * @throws LexicalException 
	 */
	public ExpressionParser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		t = lexer.next();
        
	}


	@Override
	public AST parse() throws PLCCompilerException {
		Expr e = expr();
		return e;
	}
    
    void match(Kind kind) throws LexicalException {
        if (isKind(kind)) {
            t = lexer.next();
        }
        else {
            throw new LexicalException("Parsing bug");
        }
    }

    // goes to next token
    void consume() throws LexicalException {
        t = lexer.next();
    }

    //Expr::=
    //    ConditionalExpr | LogicalOrExpr
	private Expr expr() throws PLCCompilerException {
        System.out.println("\n");
        System.out.println("expr()");
        try {
            Expr conditional = ConditionalExpr();
            //consume();
            return conditional;
        } catch (PLCCompilerException e) {System.out.println("caught ConditionalExpr");}

        try {
            Expr logicalOr = LogicalOrExpr();
            return logicalOr;
        } catch (PLCCompilerException e) {} catch (Exception e) {System.out.println("caught LogicalOrExpr");}
        /*System.out.println("Heyo " + t);
        if (isKind(EOF)) {
            throw new SyntaxException(null);
        }*/
        throw new SyntaxException(null);
	}

    //ConditionalExpr ::= ? Expr -> Expr , Expr
    Expr ConditionalExpr() throws PLCCompilerException {
        System.out.println("ConditionalExpr()");
        IToken firstToken = t;
        Expr expr1 = null, expr2 = null, expr3 = null;
        // if token is if, then get next token
        if (isKind(QUESTION)) {
            System.out.println("Question Found!!!");
            consume();
			expr1 = expr();
        }
        // if token is "(", call expression
        if (isKind(RARROW)) {
            System.out.println("Right Arrow Found!!!");
            consume();
			expr2 = expr();
        }
        // if token is ")", call expression
        if (isKind(COMMA)) {
            System.out.println("Comma Found!!!");
            consume();
            expr3 = expr();
            return new ConditionalExpr(firstToken,expr1,expr2,expr3);
        }

		
        throw new SyntaxException(null);
    }

    //LogicalOrExpr ::= LogicalAndExpr ( '|' LogicalAndExpr)*
    Expr LogicalOrExpr() throws Exception {
        System.out.println("LogicalOrExpr()");
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        IToken op = null;
        left = LogicalAndExpr();
        while(isKind(BITOR, OR)) {
            op = t;
            consume();
            right = LogicalAndExpr();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;
    }

    //LogicalAndExpr ::=
    //    ComparisonExpr ( '&'  ComparisonExpr)*
    Expr LogicalAndExpr() throws Exception {
        System.out.println("LogicalAndExpr()");
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        IToken op = null;
        left = ComparisonExpr();
        while (isKind(BITAND,AND)) {
            op = t;
            consume();
            right = ComparisonExpr();
            left = new BinaryExpr(firstToken,left,op,right);
        }
        return left;
    }
    
    //ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*
    Expr ComparisonExpr() throws Exception {
        System.out.println("ComparisonExpr()");
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        IToken op = null;
        left = PowExpr();

        while(isKind(LT,GT,EQ,LE,GE)) {
            op = t;
            consume();
            right = PowExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }

        return left;
    }
    //PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
    //(AdditiveExpr ** PowExpr) |   AdditiveExpr
    Expr PowExpr() throws Exception {
        System.out.println("ComparisonExpr()");
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        IToken op = null;
        left = AdditiveExpr();
        //consume();
        if(isKind(EXP)) {
            op = t;
            consume();
            right = PowExpr();
            consume();
            left = new BinaryExpr(firstToken, left, op, right);
            
        }
        return left;
    }

    //AdditiveExpr ::= MultiplicativeExpr ( ('+'|'-') MultiplicativeExpr )*
    Expr AdditiveExpr() throws Exception {
        System.out.println("AdditiveExpr()");
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        IToken op = null;
        left = MultiplicativeExpr();
        while(isKind(PLUS, MINUS)) {
            op = t;
            consume();
            // throw error?
            right = MultiplicativeExpr();
            left = new BinaryExpr(firstToken, left, op, right);

        }
        return left;
    }

    //MultiplicativeExpr ::= UnaryExpr (('*'|'/' | '%') UnaryExpr)*
    Expr MultiplicativeExpr() throws Exception {
        System.out.println("MultiplicativeExpr()");
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        IToken op = null;
        left = UnaryExpr();
        while(isKind(TIMES,DIV,MOD)) {
            op = t;
            consume();
            right = UnaryExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    //UnaryExpr ::= ('!'|'-'| COLOR_OP | IMAGE_OP) UnaryExpr | UnaryExprPostfix
    Expr UnaryExpr() throws SyntaxException {
        System.out.println("UnaryExpr()");
        IToken firstToken = t;
        IToken op = null;
        Expr e = null;
        try {
            if(isKind(BANG,MINUS,RES_width,RES_height)) {
                op = t;
                consume();
                e = UnaryExpr();
                return new UnaryExpr(firstToken,op,e);
            }
            else {
                return PostfixExpr();
            }
            
            
        } catch (PLCCompilerException ex) {throw new SyntaxException(null);}
        
    }

    //PostfixExpr::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε ) 
    // ε means empty, so Expr e = null;
    Expr PostfixExpr() throws PLCCompilerException {
        System.out.println("PostfixExpr()");
        IToken firstToken = t;
        Expr prim = PrimaryExpr();

        PixelSelector pix = PixelSelector();
        System.out.println("bruh " + pix);
        if (pix == null) {
            System.out.println("YOOO ");
            ChannelSelector chan = ChannelSelector();
            if (chan == null) {
                return prim;
            }
            System.out.println("no pix()");
            return new PostfixExpr(firstToken, prim, null, chan);
        }
        else {
            ChannelSelector chan = ChannelSelector();
            if (chan == null) {
                System.out.println("no chan()");
                return new PostfixExpr(firstToken, prim,pix,null);
            }
            System.out.println("nice()");
            return new PostfixExpr(firstToken, prim,pix,chan);
        }
        
    }
    
    //PrimaryExpr ::=STRING_LIT | NUM_LIT |  BOOLEAN_LIT | IDENT | ( Expr ) | CONST |  ExpandedPixelExpr
    Expr PrimaryExpr() throws PLCCompilerException {
        System.out.println("PrimaryExpr()");
        //consume();
        IToken firstToken = t;
        Expr e = null;
        System.out.println("kind " + t.kind());
        try {
        if (isKind(STRING_LIT)) {
            e = new StringLitExpr(firstToken);
            consume();
            return e;
        }
        if (isKind(NUM_LIT)) {
            e = new NumLitExpr(firstToken);
            consume();
            return e;
        }
        if (isKind(BOOLEAN_LIT)) {
            e = new BooleanLitExpr(firstToken);
            consume();
            return e;
        }
        
        if (isKind(IDENT)) {
            e = new IdentExpr(firstToken);
            consume();
            return e;
        }
        if (isKind(LPAREN)) {
            consume();
            e = expr();
            match(RPAREN); 
            //consume();
            return e;
        }
        if(isKind(CONST)) {
            e = new ConstExpr(firstToken);
            consume();
            return e;
        }
        
        Expr bigpix = ExpandedPixelExpr();
        if (bigpix != null) {
            return bigpix;
        }
        
        throw new SyntaxException(null);
        } catch(PLCCompilerException ignored) {
            throw new PLCCompilerException("Expected literal or (");
        }
        
    }
    //ChannelSelector ::= : red | : green | : blue 
    ChannelSelector ChannelSelector() throws PLCCompilerException {
        System.out.println("PixelSelector()");
        
        if(isKind(COLON)) {
            consume();
        }
        else {
            return null;
        }

        IToken firstToken = t;
        System.out.println(firstToken.kind());
        IToken color = t;
        if(isKind(RES_blue,RES_green,RES_red)) {
            consume();
            return new ChannelSelector(firstToken, color);
        }
        throw new SyntaxException(null);
    }
    
    //PixelSelector::=
    //    '[' Expr ',' Expr ']'
    PixelSelector PixelSelector() throws PLCCompilerException { // '[' Expr ',' Expr ']'
        System.out.println("PixelSelector()");
        IToken firstToken = t;
        Expr e1 = null, e2 = null;

        if(isKind(LSQUARE)) {
            System.out.println("lsquare passed");
            consume();
            e1 = expr();
        }
        else {
            return null;
        }

        if(isKind(COMMA)) {
            System.out.println("comma passed");
            consume();
            e2 = expr();
            
            match(RSQUARE);
            System.out.println("rsquare passed");
            return new PixelSelector(firstToken, e1, e2);
        }

		
        throw new SyntaxException(null);
    }

        //PixelSelector::=
    //    '[' Expr ',' Expr ']'
    ExpandedPixelExpr ExpandedPixelExpr() throws PLCCompilerException {
        System.out.println("ExpandedPixelExpr()");
        IToken firstToken = t;
        Expr e1 = null, e2 = null, e3 = null;;

        if(isKind(LSQUARE)) {
            consume();
            e1 = expr();
        }
        else {
            return null;
        }
        if(isKind(COMMA)) {
            consume();
            e2 = expr();
        }
        if(isKind(COMMA)) {
            consume();
            e3 = expr();
            match(RSQUARE);
            return new ExpandedPixelExpr(firstToken, e1, e2, e3);
        }
		throw new SyntaxException(null);
    }

}
