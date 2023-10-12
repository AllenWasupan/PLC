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

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

import static edu.ufl.cise.cop4020fa23.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Parser implements IParser {
	
	final ILexer lexer;
	private IToken t;
	
	Parser(String input) throws SyntaxException {
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
	public Parser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		t = lexer.next();
        
	}


	@Override
	public AST parse() throws LexicalException, SyntaxException {
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

	//Program::=  Type IDENT ( ParamList )   Block                         

	//Block ::=  <:  (Declaration ; | Statement ;)*  :> 

	//ParamList ::= ε |  NameDef  ( , NameDef ) * 

	//NameDef ::= Type IDENT | Type Dimension  IDENT                    
	NameDef NameDef() throws SyntaxException {
        System.out.println("NameDef()");
        NameDef namedef = null;
        IToken firstToken = t;
        IToken type = null;
        IToken name = null;
        Dimension dim = null;
        //System.out.println(t.getText() + " && " + t.getKind());
        if(isKind(TYPE)) {
            // idk if i need to do consume()
            //System.out.println("189" + t.getKind());
            type = t;
            consume();
            //System.out.println("192" + t.getKind());
            // if there is an IDENT
            if(t.getKind() == IDENT) {
                name = t; // then gets the "name"?
                //System.out.println("196 if(t.getKind() == IDENT)");
                namedef = new NameDef(firstToken,type,dim, name);
            }
            // else if dimension exists
            else if ((dim = Dimension()) != null) {
                //dim = Dimension();
                //System.out.println("202 else if ((dim = Dimension()) != null)");
                consume();
                type = t;
                namedef = new NameDef(firstToken,type,dim,type);
            }
        }
        //System.out.println("hi");
        return namedef;
    }

	//Type ::= image | pixel | int | string | void | boolean 
	Expr Type() throws LexicalException, SyntaxException {
		System.out.println("Type()");
        //consume();
        IToken firstToken = t;
        Expr e = null;
        System.out.println("kind " + t.kind());
        try {
        if (isKind(image)) {
            e = new StringLitExpr(firstToken);
            consume();
            return e;
        }
        if (isKind(pixel)) {
            e = new NumLitExpr(firstToken);
            consume();
            return e;
        }
        if (isKind(int)) {
            e = new BooleanLitExpr(firstToken);
            consume();
            return e;
        }
        
        if (isKind(string)) {
            e = new IdentExpr(firstToken);
            consume();
            return e;
        }
        if (isKind(void)) {
            consume();
            e = expr();
            match(RPAREN); 
            //consume();
            return e;
        }
        if(isKind(boolean)) {
            e = new ConstExpr(firstToken);
            consume();
            return e;
        }
        
        Expr bigpix = ExpandedPixelExpr();
        if (bigpix != null) {
            return bigpix;
        }
        
        throw new SyntaxException(null);
        } catch(SyntaxException ignored) {
            throw new SyntaxException("Expected literal or (");
        }
	}
	//Declaration::= NameDef |  NameDef = Expr

    //Expr::= ConditionalExpr | LogicalOrExpr
	private Expr expr() throws SyntaxException, LexicalException {
        System.out.println("\n");
        System.out.println("expr()");
        try {
            Expr conditional = ConditionalExpr();
            //consume();
            return conditional;
        } catch (SyntaxException e) {System.out.println("caught ConditionalExpr");}

        try {
            Expr logicalOr = LogicalOrExpr();
            return logicalOr;
        } catch (SyntaxException e) {System.out.println("caught LogicalOrExpr");}

        consume();
        throw new SyntaxException(null);
	}

    //ConditionalExpr ::= ? Expr -> Expr , Expr
    Expr ConditionalExpr() throws SyntaxException, LexicalException {
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
    Expr LogicalOrExpr() throws SyntaxException, LexicalException {
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

    //LogicalAndExpr ::= ComparisonExpr ( '&'  ComparisonExpr)*
    Expr LogicalAndExpr() throws SyntaxException, LexicalException {
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
    Expr ComparisonExpr() throws SyntaxException, LexicalException {
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
    Expr PowExpr() throws SyntaxException, LexicalException {
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
    Expr AdditiveExpr() throws SyntaxException, LexicalException {
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
    Expr MultiplicativeExpr() throws SyntaxException, LexicalException {
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
    Expr PostfixExpr() throws SyntaxException,LexicalException {
        System.out.println("PostfixExpr()");
        IToken firstToken = t;
        
        Expr prim = PrimaryExpr();
        
        PixelSelector pix = PixelSelector();
        if (pix == null) {
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
    Expr PrimaryExpr() throws SyntaxException, LexicalException {
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
        } catch(SyntaxException ignored) {
            throw new SyntaxException("Expected literal or (");
        }
        
    }
    //ChannelSelector ::= : red | : green | : blue 
    ChannelSelector ChannelSelector() throws SyntaxException, LexicalException {
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
    
    //PixelSelector::= '[' Expr ',' Expr ']'
    PixelSelector PixelSelector() throws SyntaxException, LexicalException { // '[' Expr ',' Expr ']'
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

        //PixelSelector::= '[' Expr ',' Expr ']'
    ExpandedPixelExpr ExpandedPixelExpr() throws SyntaxException, LexicalException {
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

	//PixelSelectorInLValue  ::= [ Expr , Expr ] 
	PixelSelector PixelSelectorInLValue() throws LexicalException, SyntaxException {
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

	//Dimension  ::=  [ Expr , Expr ]                     
	Dimension Dimension() throws LexicalException, SyntaxException {
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
            return new Dimension(firstToken, e1, e2);
        }

        throw new SyntaxException(null);
	}

	//LValue ::=  IDENT (PixelSelector | ε ) (ChannelSelector | ε ) 

	
	//Statement::= 
	/* 
	LValue = Expr | 
				write Expr | 
				do GuardedBlock [] GuardedBlock* od  | 
				if   GuardedBlock [] GuardedBlock*  if    | 
	^ Expr  | 
				BlockStatement  | */

	//GuardedBlock := Expr -> Block 

	//BlockStatement ::= Block

}

