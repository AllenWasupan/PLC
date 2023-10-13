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
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

import static edu.ufl.cise.cop4020fa23.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.plaf.nimbus.State;


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
        try {
            Expr e = expr();
            return e;
        } catch(SyntaxException error) {Program p = Program(); return p;}
		
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
    Program Program() throws LexicalException, SyntaxException {
        System.out.println("Program()");
        IToken firstToken = t;
        IToken type = null;
        IToken name = null;
        List<NameDef> params = null;
        Block b = null;
        
        type = Type();

        if (isKind(IDENT)) {
            name = t;
            consume();
        }

        match(LPAREN);

        params = ParamList();

        match(RPAREN);
        System.out.println("P block");
        b = Block();
        consume();
        return new Program(firstToken, type, name, params, b);
        
    }
	//Block ::=  <:  (Declaration ; | Statement ;)*  :> 
    Block Block() throws SyntaxException, LexicalException {
        System.out.println("Block()");
        IToken firstToken = t;
        Declaration d = null;
        Statement s = null;
        List<BlockElem> lis = new ArrayList<BlockElem>();
        BlockElem next = null;
        while (isKind(SEMI,BLOCK_OPEN)) {
            consume();
            d = Declaration();
            System.out.println(d);
            if (d.getNameDef() == null) {
                s = Statement();
                System.out.println(s);
                if (s == null) {
                    
                    break;
                }
                else {
                    next = s;
                }
            }
            else {
                next = d;
            }        
            lis.add(next);
            System.out.println("Adding Block " + next);
        }

        match(BLOCK_CLOSE);
        System.out.println(lis);
        return new Block(firstToken, lis);
    
    }
	//ParamList ::= ε |  NameDef  ( , NameDef ) * 
    List<NameDef> ParamList() throws LexicalException, SyntaxException {
        System.out.println("ParamList()");
        IToken firstToken = t;
        NameDef next = null;
        IToken op = null;
        List<NameDef> l = new ArrayList<NameDef>();
        int count = 0;
        boolean start = false;

        while (isKind(COMMA) || start == false) {
            start = true;
            if (isKind(COMMA)) {
                consume();
            }
            next = NameDef();
            if (next == null) {
                break;
            }
            l.add(next);
            System.out.println("Adding Param " + next);
            System.out.println(t);
        }
        System.out.println();
        l.forEach(System.out::println);;
        return l;
    }

	//NameDef ::= Type IDENT | Type Dimension  IDENT                    
	NameDef NameDef() throws SyntaxException, LexicalException {
        System.out.println("NameDef()");
        IToken firstToken = t;
        IToken type = null;
        IToken token = null;
        Dimension dim = null;
        System.out.println(t);
        try {
            if (isKind(RES_image,RES_pixel,RES_int,RES_string,RES_void,RES_boolean)) {
                System.out.println("NameDefType");
                System.out.println(t);
                type = Type();
                System.out.println(t);
                if (isKind(IDENT)) {
                    token = t;
                    consume();
                }
                else {
                    dim = Dimension();
                    token = t;
                    match(IDENT);
                }
                return new NameDef(firstToken,type,dim,token);
            }
            
        }catch(SyntaxException ignored) {throw new SyntaxException(null);}catch(LexicalException ignored) {throw new SyntaxException(null);}
        return null;
        
    }

	//Type ::= image | pixel | int | string | void | boolean 
	IToken Type() throws LexicalException, SyntaxException {
		System.out.println("Type()");
        //consume();
        IToken firstToken = t;
        try {
        if (isKind(RES_image)) {
            consume();
            return firstToken;
        }
        if (isKind(RES_pixel)) {
            consume();
            return firstToken;
        }
        if (isKind(RES_int)) {
            consume();
            return firstToken;
        }
        if (isKind(RES_string)) {
            consume();
            return firstToken;
        }
        if (isKind(RES_void)) {
            consume();
            return firstToken;
        }
        if(isKind(RES_boolean)) {
            consume();
            return firstToken;
        }
        System.out.println(t);
        System.out.println("bye");
        throw new SyntaxException(null);
        } catch(SyntaxException ignored) {throw new SyntaxException("Type error");}
	}
	//Declaration::= NameDef |  NameDef = Expr
    Declaration Declaration() throws SyntaxException, LexicalException {
        IToken firstToken = t;
        NameDef n = null;
        Expr e = null;
        n = NameDef();
        if (isKind(ASSIGN)) {
            e = expr();
        }
        return new Declaration(firstToken, n, e);
        
    }
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

        System.out.println("Thrown from expr");
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
		System.out.println("Dimension()");
        IToken firstToken = t;
        Expr e1 = null, e2 = null;

        if(isKind(LSQUARE)) {
            System.out.println("lsquare passed");
            consume();
            e1 = expr();
            System.out.println("uh");
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
    LValue LValue() throws SyntaxException, LexicalException {
        System.out.println("LValue()");
        IToken firstToken = t;
        System.out.println("first token " + firstToken);
        match(IDENT);
        System.out.println(t);
        PixelSelector pix = PixelSelector();
        if (pix == null) {
            ChannelSelector chan = ChannelSelector();
            if (chan == null) {
                System.out.println("LValue all null");
                return new LValue(firstToken, firstToken, null, null);
            }
            System.out.println("no pix()");
            return new LValue(firstToken, firstToken, null, chan);
        }
        else {
            ChannelSelector chan = ChannelSelector();
            if (chan == null) {
                System.out.println("no chan()");
                return new LValue(firstToken, firstToken,pix,null);
            }
            System.out.println("nice()");
            
            return new LValue(firstToken, firstToken,pix,chan);
        }
        
    }
	
	//Statement::= 
/* 
	LValue = Expr | 
    write Expr | 
    do GuardedBlock [] GuardedBlock* od  | 
    if   GuardedBlock [] GuardedBlock*  if    | 
	^ Expr  | 
    BlockStatement  | 
*/

    Statement Statement() throws LexicalException, SyntaxException {
        System.out.println("Statement()");
        IToken firstToken = t;
        Expr e = null;
        LValue l = null;
        Block bl = null;
        GuardedBlock gbl = null;
        List<GuardedBlock> listbl = null;

        try {
            System.out.println(t);
            l = LValue();
            System.out.println(t);
            match(ASSIGN);
            System.out.println(t);
            e = expr();
            
            return new AssignmentStatement(firstToken,l,e);
        } catch (LexicalException ee) {System.out.println("caught AssignmentStatement");} catch(SyntaxException aa) {System.out.println("caught AssignmentStatement");}

        try {
            match(RES_write);
            e = expr();
            return new WriteStatement(firstToken,e);
        } catch (LexicalException ee) {System.out.println("caught WriteStatement");} catch(SyntaxException aa) {System.out.println("caught WriteStatement");}

        try {
            match(RES_do);
            gbl = GuardedBlock();
            match(BOX);
            gbl = GuardedBlock();
            match(RES_od);
            return new DoStatement(firstToken,listbl); 
        } catch (LexicalException ee) {System.out.println("caught DoStatement");} catch(SyntaxException aa) {System.out.println("caught DoStatement");}

        try { 
            match(RES_if);
            gbl = GuardedBlock();
            match(BOX);
            gbl = GuardedBlock();
            match(RES_od);
            match(RES_fi);
            return new IfStatement(firstToken,listbl);
        } catch (LexicalException ee) {System.out.println("caught ReturnStatement");} catch(SyntaxException aa) {System.out.println("caught ReturnStatement");}

        try {
            match(RETURN);
            e = expr();
            return new ReturnStatement(firstToken,e);
        } catch (LexicalException ee) {System.out.println("caught ReturnStatement");} catch(SyntaxException aa) {System.out.println("caught ReturnStatement");}

        try {
            match(BLOCK_OPEN);
            bl = BlockStatement();
            return new StatementBlock(firstToken,bl);
        } catch (LexicalException ee) {System.out.println("caught BlockStatement");}catch(SyntaxException aa) {System.out.println("caught BlockStatement");}

        return null;

    }

	//GuardedBlock := Expr -> Block 
    GuardedBlock GuardedBlock() throws LexicalException, SyntaxException {
        System.out.println("GuardedBlock()");
        IToken firstToken = t;
        Expr e = null;
        Block bl = null;

        e = expr();
        consume();
        match(RARROW);
        bl = Block();
        consume();

        return new GuardedBlock(firstToken, e, bl);
    }
	//BlockStatement ::= Block
    Block BlockStatement() throws SyntaxException, LexicalException {
        System.out.println("BlockStatement()");
        return Block();
    }
}

