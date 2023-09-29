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
	
	ExpressionParser(String input) {
        lexer = ComponentFactory.makeLexer(input);
    }
	protected boolean isKind(Kind kind) {
		return t.kind() == kind;
	}
	
	protected boolean isKind(Kind... kinds) {
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

    //Expr::=
    //    ConditionalExpr | LogicalOrExpr
	private Expr expr() throws PLCCompilerException {
        try {
            Expr conditional = ConditionalExpr();
            //consume();
            return conditional;
        } catch (PLCCompilerException e) {}

        try {
            Expr logicalOr = LogicalOrExpr();
            return logicalOr;
        } catch (PLCCompilerException e) {} catch (Exception e) {

        }
        return null;
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
	/* 
    Program Program() {
        System.out.println("Program()");
        IToken firstToken = t;
        Program prog = null;
        NameDef name = null;
        NameDef name1 = null;
        NameDef name2 = null;
        Declaration dec = null;
        Statement state = null;
        ArrayList<NameDef> params = new ArrayList<NameDef>();
        List<ASTNode> decsAndStates = new ArrayList<ASTNode>();
        Types ah = null;
        try {
            if (isKind(TYPE, RES_void)) {
                IToken op = t; // get type
                consume();
                IToken ident = t; // get ident
                consume();
                //System.out.println(Types.Type.toType(op.getText()));
                //System.out.println(ident.getText());
                match(LPAREN);
                // check if NameDef exists
                //name = NameDef(); // this may break things if it gives error
                //System.out.println("hi");
                //if ((name = NameDef()) != null) {
                if ((name = NameDef()) != null) {
                    System.out.println("141" + name.getType());
                    params.add(name);
                    // if NameDef does exist
                    consume();

                    while(isKind(COMMA)) {
                        consume();
                        //System.out.println("147" + name);
                        name1 = NameDef();
                        //System.out.println("147" + name1.getType());
                        params.add(name1);
                        consume();
                    }
                }
                match(RPAREN);
                int count = 0;
                do {
                    if(count>0) {consume();}
                    if((dec = Declaration()) != null) {
                        System.out.println("160 in dec");
                        decsAndStates.add(dec);
                        consume();
                        System.out.println(t.getText() + " & " + t.getKind());
                        count++;
                    }
                    else if ((state = Statement()) != null) {
                        System.out.println("165 in state");
                        decsAndStates.add(state);
                        consume();
                        count++;
                    }

                } while (!isKind(SEMI));


                decsAndStates.remove(decsAndStates.size()-1);
                prog = new Program(firstToken,Types.Type.toType(op.getText()),ident.getText(),params,decsAndStates);
            }

        } catch (PLCCompilerException e) {}

        return prog;
    }

    //    NameDef::=
    //    Type IDENT | //yields NameDef
    //    Type Dimension IDENT //yields NameDefWithDimension
    NameDef NameDef() throws PLCCompilerException {
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
                namedef = new NameDef(firstToken,type,name);
            }
            // else if dimension exists
            else if ((dim = Dimension()) != null) {
                //dim = Dimension();
                //System.out.println("202 else if ((dim = Dimension()) != null)");
                consume();
                type = t;
                namedef = new NameDefWithDim(firstToken,type,type,dim);
            }
        }
        //System.out.println("hi");
        return namedef;
    }

    //    Declaration::=
    //    NameDef (('=' | '<-') Expr)?  //yields VarDeclaration
    VarDeclaration Declaration() throws PLCCompilerException {
        System.out.println("Declaration()");
        VarDeclaration varDec = null;
        IToken firstToken = t;
        IToken op = null;
        Expr right = null;
        //consume();
        System.out.println(t.getText() + " 1& " + t.getKind());
        NameDef left = NameDef();
        //consume();
        if(isKind(ASSIGN, LARROW)) {
            op = t;
            System.out.println(t.getText() + " && " + t.getKind());
            consume();
            right = expr();
            return new VarDeclaration(firstToken, left, op, right);// idk what im doing with this line
        }
        return new VarDeclaration(firstToken, left, op, right);
    }
*/
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

        }
		if (expr1 == null || expr2 == null || expr3 == null) {
			throw new PLCCompilerException("ConditionalExpr failed to find");
		}
		return new ConditionalExpr(firstToken,expr1,expr2,expr3);
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

    //ComparisonExpr ::=
    //    AdditiveExpr ( ('<' | '>' | '==' | '!=' | '<=' | '>=') AdditiveExpr)*
    Expr ComparisonExpr() throws Exception {
        System.out.println("ComparisonExpr()");
        IToken firstToken = t;
        Expr left = null;
        Expr right = null;
        IToken op = null;
        left = AdditiveExpr();
        while(isKind(LT,GT,EQ,LE,GE)) {
            op = t;
            consume();
            right = AdditiveExpr();
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
    Expr UnaryExpr() throws Exception {
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
            throw new LexicalException("Expected int literal or (");

        } catch (PLCCompilerException ex) {}

        try {
            return PostfixExpr(); //FUDGE UNARYEXPRPOSTFIX
        } catch (LexicalException ex) {}

        throw new LexicalException("Expected int literal or (");

    }

    //PostfixExpr::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε ) 
    // ε means empty, so Expr e = null;
    /*
        get primaryExpr
        get pixelSelector
        if pixelSelector is not valid
            get channelSelector
            if channelSelector not valid
                return primaryExpr
            else return PostFixExpr(primaryExpr, null, channelSelector)
        otherwise pixelSelector is valid
        get channelSelector
        if channelSelector not valid
        return PostFixExpr(primaryExpr, pixelSelector, null)
        else
        return PostFixExpr(primaryExpr, pixelSelector, channelSelector)
     */
    Expr PostfixExpr() throws PLCCompilerException {
        System.out.println("PostfixExpr()");
        IToken firstToken = t;
        Expr e = PrimaryExpr();
        return e;
    }
    
    //PrimaryExpr ::=STRING_LIT | NUM_LIT |  BOOLEAN_LIT | IDENT | ( Expr ) | CONST |  ExpandedPixelExpr
    Expr PrimaryExpr() throws PLCCompilerException {
        System.out.println("PrimaryExpr()");
        //consume();
        IToken firstToken = t;
        Expr e = null;
        Expr e1 = null;
        Expr e2 = null;

        try {
        if (isKind(STRING_LIT)) {
            e = new StringLitExpr(firstToken);
            consume();
        }
        if (isKind(NUM_LIT)) {
            e = new NumLitExpr(firstToken);
            consume();
        }
        if (isKind(BOOLEAN_LIT)) {
            e = new BooleanLitExpr(firstToken);
            consume();
        }
        if (isKind(IDENT)) {
            e = new IdentExpr(firstToken);
            consume();
        }
        if (isKind(LPAREN)) {
            consume();
            e = expr();
            match(RPAREN);
            consume();
        }
        if(isKind(CONST)) {
            e = new ConstExpr(firstToken);
            consume();
        }
        System.out.println("bruh()");
        System.out.println("e " + e);
        return e;

        } catch(PLCCompilerException ignored) {
            throw new PLCCompilerException("Expected literal or (");
        }
        
    }

    //PixelSelector::=
    //    '[' Expr ',' Expr ']'
    PixelSelector PixelSelector() throws PLCCompilerException { // '[' Expr ',' Expr ']'
        System.out.println("PixelSelector()");
        IToken firstToken = t;
        Expr e1 = null, e2 = null;

        if(isKind(LSQUARE)) {
            e1 = expr();
            consume();
        }
        if(isKind(COMMA)) {
            e2 = expr();
            consume();
        }
        if(isKind(RSQUARE)) {
            return new PixelSelector(firstToken, e1, e2);
        }
        throw new PLCCompilerException("PixelSelector Error");
    }
/* 
    //    Dimension::=
    //            '[' Expr ',' Expr ']' //yields Dimension
    Dimension Dimension() throws PLCCompilerException {
        System.out.println("Dimension()");
        IToken firstToken = t;
        Expr width = null, height = null;

        if(isKind(LSQUARE)) {
            width = expr();
            consume();
        }
        if(isKind(COMMA)) {
            height = expr();
            consume();
        }
        if(isKind(RSQUARE)) {
            return new Dimension(firstToken, width, height);
        }
        throw new PLCCompilerException("Dimension Error");
    }

    //    Statement::=
    //    IDENT PixelSelector? '=' Expr | //yields AssignmentStatement
    //    IDENT PixelSelector? ‘<-’ Expr| //yields ReadStatement
    //            'write' Expr '->' Expr| //yields WriteStatement
    //            '^' Expr                //yields ReturnStatement
    Statement Statement() throws PLCCompilerException {
        System.out.println("Statement()");
        IToken firstToken = t;
        PixelSelector pixSelect = null;
        String name = null;
        Expr e = null;
        Expr e1 = null;
        Statement statement = null;
        if(isKind(IDENT)) {
            pixSelect = PixelSelector();
            if(pixSelect != null) {
                //consume();
                if(isKind(ASSIGN)) {
                    e = Expr();
                    statement = new AssignmentStatement(firstToken,name,pixSelect,e);
                }
                else if (isKind(LARROW)) {
                    e = Expr();
                    statement = new ReadStatement(firstToken,name,pixSelect,e);
                }
                else {
                    throw new SyntaxException("Statement() expected '=' or '<-'");
                }
            }
        }
        else if (isKind(RES_write)) {
            e = expr();
            match(RARROW);
            e1 = expr();
            statement = new WriteStatement(firstToken, e, e1);
        }
        else if (isKind(RETURN)) {
            e = expr();
            statement = new ReturnStatement(firstToken, e);
        }
        return statement;
    }
*/
}
/*
abstract class ASTNode
{
    public abstract Object visit(ASTVisitor v, Object arg);

}*/