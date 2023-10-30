package edu.ufl.cise.cop4020fa23;

import java.util.List;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.ASTVisitor;
import edu.ufl.cise.cop4020fa23.ast.AssignmentStatement;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.Block;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.Declaration;
import edu.ufl.cise.cop4020fa23.ast.Dimension;
import edu.ufl.cise.cop4020fa23.ast.DoStatement;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.GuardedBlock;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.IfStatement;
import edu.ufl.cise.cop4020fa23.ast.LValue;
import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.Program;
import edu.ufl.cise.cop4020fa23.ast.ReturnStatement;
import edu.ufl.cise.cop4020fa23.ast.StatementBlock;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.Type;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.ast.WriteStatement;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable st;
	Program root;

	TypeCheckVisitor() {
		System.out.println("TypeCheckVisitor");
		st = new SymbolTable();
		
	}

	private void check(boolean b, Object object, String string) throws TypeCheckException {
		System.out.println("check");
		if (b == false) {
			throw new TypeCheckException();
		}
	}

	AST typeCheck(String input) throws PLCCompilerException {
		System.out.println("typeCheck");
		AST ast = ComponentFactory.makeParser(input).parse();
		ASTVisitor typeChecker = ComponentFactory.makeTypeChecker();
		ast.visit(typeChecker, null);
		return ast;
	}

	//Program::=  Type IDENT  NameDef*  Block 
	@Override
	public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
		System.out.println("visitProgram");
		root = program;
		Type type = Type.kind2type(program.getTypeToken().kind());
		program.setType(type);
		st.enterScope();
		List<NameDef> params = program.getParams();
		for (NameDef param : params) {
			param.visit(this, arg);
		}
		program.getBlock().visit(this, arg);
		st.leaveScope();
		return type;
	}

	//Block ::=  (Declaration  | Statement )*
	@Override
	public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
		System.out.println("visitBlock");
		st.enterScope();
		List<BlockElem> blockElems = block.getElems();
		for (BlockElem elem : blockElems) {
			elem.visit(this, arg);
		}
		st.leaveScope();
		return block;
	}

	//NameDef ::= Type Dimension? IDENT
	/*
	 Condition:   if (Dimension != null) { type == IMAGE } 
                   else Type ∈ {INT, BOOLEAN, STRING, PIXEL, IMAGE} 
					NameDef.type  type 
					symbolTable.insert(nameDef)  is successful
	 */
	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
		System.out.println("\nvisitNameDef");
		Type type = nameDef.getType();
		Dimension d = nameDef.getDimension();
		
		System.out.println("nameDef " + nameDef);
		if (d != null) {
			d.visit(this, arg);
			type = Type.IMAGE;
			st.insert(nameDef);
			return type;
		}
		else if (type == Type.INT || type == Type.BOOLEAN || type == Type.STRING || type == Type.PIXEL || type == Type.IMAGE) {
			type = Type.kind2type(nameDef.getTypeToken().kind());
			nameDef.setType(type);
			st.insert(nameDef);
			return type;
		}
			

		throw new TypeCheckException("visitNameDef");
	}

	/*
	 *Declaration::= NameDef Expr?   
            Condition:  Expr == null   
                           ||  Expr.type == NameDef.type  
                           ||  (Expr.type == STRING && NameDef.type == IMAGE) 
             Declaration.type  NameDef.type 
            Note:  visit Expr before NameDef
	 */
	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
		System.out.println("visitDeclaration");
		System.out.println(declaration);
		Type type;
		
		Expr e = declaration.getInitializer();
		NameDef n = declaration.getNameDef();
		n.visit(this, arg);
		
		if (e != null) {
			e.visit(this, arg);
			System.out.println("E " + e);
			System.out.println("Type " + e.getType());
		}
		
		System.out.println("N " + n);
		if (e == null || e.getType() == n.getType() || (e.getType() ==  Type.STRING && n.getType() == Type.IMAGE)) {
			type = n.getType();
			n.setType(type);
			
			return declaration;
		}
		//System.out.println("fail null");
		//return null;
		throw new TypeCheckException("visitDeclaration");
	}

	/*
	 * ConditionalExpr ::=  ExprguardExpr    
                                 ExprtrueExpr     
                                 ExprfalseExpr     
              Condition:  ExprguardExpr.type  ==  BOOLEAN 
              Condition:  ExprtrueExpr.type == ExprfalseExpr.type 
              ConditionalExpr.type  trueExpr.type
	 */
	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
		System.out.println("visitConditionalExpr");
		Type typeG = (Type) conditionalExpr.getGuardExpr().visit(this, arg);
		check(typeG == Type.BOOLEAN, conditionalExpr, "image width must be bool");
		Type typeT = (Type) conditionalExpr.getTrueExpr().visit(this, arg);
		check(typeT == conditionalExpr.getFalseExpr().getType(), conditionalExpr, "image width must be bool");
		conditionalExpr.setType(typeT);
		return conditionalExpr;
	}
	
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
		Type infertype = null;
		Type typeL = (Type) binaryExpr.getLeftExpr().visit(this, arg);
		Kind Op = binaryExpr.getOpKind();
		Type typeR = (Type) binaryExpr.getRightExpr().visit(this, arg);
		if (Op == Kind.EQ) {
			if (typeR == typeL) {
				infertype = Type.BOOLEAN;
			}
		}

		if (Op == Kind.PLUS) {
			if (typeR == typeL) {
				infertype = typeL;
			}
		}

		if (typeL == Type.PIXEL) {
			if (Op == Kind.BITAND || Op == Kind.BITOR) {
				if (typeR == Type.PIXEL) {
					infertype = Type.PIXEL;
				}
			}

			if (Op == Kind.EXP) {
				if (typeR == Type.INT) {
					infertype = Type.PIXEL;
				}
			}

			if (Op == Kind.MINUS ||Op == Kind.TIMES || Op == Kind.DIV || Op == Kind.MOD) {
				if (typeR == typeL) {
					infertype = typeL;
				}
			}
			if (Op == Kind.TIMES || Op == Kind.DIV || Op == Kind.MOD) {
				if (typeR == Type.INT) {
					infertype = typeL;
				}
			}
		}

		if (typeL == Type.BOOLEAN) {
			if (Op == Kind.AND || Op == Kind.OR) {
				if (typeR == Type.BOOLEAN) {
					infertype = Type.BOOLEAN;
				}
			}
		}

		if (typeL == Type.INT) {
			if (Op == Kind.LT || Op == Kind.GT || Op == Kind.LE || Op == Kind.GE) {
				if (typeR == Type.INT) {
					infertype = Type.BOOLEAN;
				}
			}
			if (Op == Kind.EXP) {
				if (typeR == Type.INT) {
					infertype = Type.INT;
				}
			}
			if (Op == Kind.MINUS ||Op == Kind.TIMES || Op == Kind.DIV || Op == Kind.MOD) {
				if (typeR == typeL) {
					infertype = typeL;
				}
			}
		}

		if (typeL == Type.IMAGE) {

			if (Op == Kind.MINUS ||Op == Kind.TIMES || Op == Kind.DIV || Op == Kind.MOD) {
				if (typeR == typeL) {
					infertype = typeL;
				}
			}
			if (Op == Kind.TIMES || Op == Kind.DIV || Op == Kind.MOD) {
				if (typeR == Type.INT) {
					infertype = typeL;
				}
			}
		}
		
		if (infertype != null) {
			binaryExpr.setType(infertype);
			return binaryExpr;
		}
		
		throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitBinaryExpr invoked.");
	}

	/*
	 * UnaryExpr ::=  op Expr 
		Condition:  inferUnaryExpr is defined  
		UnaryExpr.type  inferUnaryExprType(Expr.type, op,)
	 */
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
		System.out.println("visitUnaryExpr");
		Type type = null;

		Type e = unaryExpr.getExpr().getType();		
		Kind op = unaryExpr.getOp();

		if (e == Type.BOOLEAN) {
			if (op == Kind.BANG) {
				type = Type.BOOLEAN;
			}
		}
		if (e == Type.INT) {
			if (op == Kind.MINUS) {
				type = Type.INT;
			}
		}
		if (e == Type.IMAGE) {
			if (op == Kind.RES_width ||op == Kind.RES_height) {
				type = Type.INT;
			}
		}
		if (type == null) {
			throw new TypeCheckException("visitUnaryExpr");
		}
		unaryExpr.setType(type);
		return unaryExpr;
	}

	/*
	 * Condition:  inferPostfixExprType is defined 
		PostfixExpr.type  inferPostfixExprType(Epxr.type, PixelSelector, ChannelSelector)
	 */
	@Override
	public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
		System.out.println("visitPostfixExpr");
		Type type;
		Type e = postfixExpr.getType();
		PixelSelector p = postfixExpr.pixel();
		ChannelSelector c = postfixExpr.channel();
		if (p == null && c == null) {
			type = e;
			postfixExpr.setType(type);
			return postfixExpr;
		}
		if (e == Type.IMAGE) {
			if (p != null) {
				if (c == null) {
					type = Type.PIXEL;
					postfixExpr.setType(type);
					return postfixExpr;
				}
				else {
					type = Type.INT;
					postfixExpr.setType(type);
					return postfixExpr;
				}
			}
			else {
				if (c != null) {
					type = Type.IMAGE;
					postfixExpr.setType(type);
					return postfixExpr;
				}
			}
		}
		else if (e == Type.PIXEL) {
			if (p == null && c != null) {
				type = Type.INT;
				postfixExpr.setType(type);
				return postfixExpr;
			}
		}
		throw new TypeCheckException("visitPostfixExpr");
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
		System.out.println("visitStringLitExpr");
		Type type = Type.STRING;
		stringLitExpr.setType(type);
		return type;
	}

	/*
	 * 
	 */
	@Override
	public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
		System.out.println("visitNumLitExpr");
		Type type = Type.INT;
		numLitExpr.setType(type);
		return type;
	}
	
	/*
	 * IdentExpr  
		Condition:  symbolTable.lookup(IdentExpr.name) defined 
		IdentExpr.nameDef  symbolTable.lookup(IdentExpr.name) 
		IdentExpr.type  IdentExpr.nameDef.type
	 */
	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
		System.out.println("visitIdentExpr");
		String name = identExpr.getName();
		 
		if (st.lookup(name) != null) {
			identExpr.setType(identExpr.getNameDef().getType());
			return identExpr;
		}
		throw new TypeCheckException("Unimplemented method 'visitIdentExpr'");
	}

	/*
	 * ConstExpr 
 		ConstExpr.type  if (ConstExpr.name == ‘Z’) INT else PIXEL
	 */
	@Override
	public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
		System.out.println("visitConstExpr");
		Type type = null;
		String name = constExpr.getName();
		if (name.equals("Z")) {
			type = Type.INT;
			constExpr.setType(type);
			return type;
		}
		else {
			type = Type.PIXEL;
			constExpr.setType(type);
			return type;
		}
		
		//throw new TypeCheckException("visitConstExpr");
	}
	/*
	 * BooleanLitExpr  
		 BooleanLitExpr.type <- BOOLEAN 
	 */
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
		System.out.println("visitBooleanLitExpr");
		Type type = Type.BOOLEAN;
		booleanLitExpr.setType(type);
		return type;
	}

	@Override
	public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
		System.out.println("visitChannelSelector");
		// TODO Auto-generated method stub
		return channelSelector;
		//throw new UnsupportedOperationException("Unimplemented method 'visitChannelSelector'");
	}

	/*
	 * PixelSelector  ::= ExprxExpr  ExpryExpr     (see discussion below) 
           If the PixelSelector’s parent is an LValue then  
			Condition:  ExprxExpr  is an IdentExp or NumLitExpr 
			Condition:  ExpryExpr    is an IdentExp or NumLitExpr 
			If ExprxExpr  is an IdentExp and symbolTable.lookup(ExprxExpr.name == null)  
			Insert a SyntheticNameDef with name ExprxExpr.name  
						and type INT into the symbol table 
			end if 
			If ExpryExpr  is an IdentExp and symbolTable.lookup(ExpryExpr.name == null)  
			Insert a SyntheticNameDef with name ExpryExpr.name  
			and type INT into the symbol table 
			end if 
			end if 
			
			Condition:  ExprxExpr.type  == INT 
			Condition:  ExpryExpr.type  == INT
	 */
	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
		System.out.println("visitPixelSelector");
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitPixelSelector'");
	}

	/*
	 *
	 * ExpandedPixelExpr ::= Exprred Exprgreen Exprblue 
		Condition:  Exprred.type  == INT 
		Condition:  Exprgreen.type  == INT 
		Condition:  Exprblue.type  == INT 
		ExpandedPixelExpr.type  PIXEL
	 */
	@Override
	public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
		System.out.println("visitExpandedPixelExpr");
		Type typeR = (Type) expandedPixelExpr.getRed().visit(this, arg);
		check(typeR == Type.INT, expandedPixelExpr, "image red must be int");
		Type typeG = (Type) expandedPixelExpr.getGreen().visit(this, arg);
		check(typeG == Type.INT, expandedPixelExpr, "image green must be int");
		Type typeB = (Type) expandedPixelExpr.getBlue().visit(this, arg);
		check(typeB == Type.INT, expandedPixelExpr, "image blue must be int");
		expandedPixelExpr.setType(Type.PIXEL);
		return expandedPixelExpr;
	}
	
	//Dimension  ::=  Expr_width  Expr_height
	/*
	Condition: Exprwidth.type == INT
	Condition: Exprheight.type == INT
	 */
	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
		System.out.println("visitDimension");
		Type typeW = (Type) dimension.getWidth().visit(this, arg);
		check(typeW == Type.INT, dimension, "image width must be int");
		Type typeH = (Type) dimension.getHeight().visit(this, arg);
		check(typeH == Type.INT, dimension, "image height must be int");
		return dimension;
	}

	/*
	 * LValue ::=  IDENTnameToken PixelSelector? ChannelSelector? 
		LValue.nameDef symbolTable.lookup(name) 
		LValue.varType  LValue.nameDef.type 
		Condition:  if (PixelSelector != null) LValue.varType == IMAGE 
		Condition:  if (ChannelSelector != null) LValue.varType ∈ { PIXEL, IMAGE} 
		Condition:  inferLValueType is defined 
		LValue.type    inferLValueType
	 */
	@Override
	public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
		System.out.println("visitLValue");
		Type type;
		NameDef n = lValue.getNameDef();
		PixelSelector p = lValue.getPixelSelector();
		ChannelSelector c = lValue.getChannelSelector();
		Type v = lValue.getVarType();
		lValue.setNameDef(st.lookup(lValue.getName()));
		lValue.setType(n.getType());
		type = n.getType();
		if (p != null) {
			if (v != Type.IMAGE) {
				throw new TypeCheckException("visitLValue");
			}
			
		}
		if (c != null) {
			if (v != Type.PIXEL && v != Type.IMAGE) {
				throw new TypeCheckException("visitLValue");
			}
		}
		//Row 1
		if (p == null && c == null) {
			type = v;
			lValue.setType(type);
			return type;
		}
		//Row 4 5
		if (p == null) {
			if (v == Type.IMAGE) {
				type = Type.IMAGE;
				lValue.setType(type);
				return type;
			}
			if (v == Type.PIXEL) {
				type = Type.INT;
				lValue.setType(type);
				return type;
			}
		}
		//Row 2
		if (c == null) {
			if (v == Type.IMAGE) {
				type = Type.PIXEL;
				lValue.setType(type);
				return type;
			}
			
		}
		//Row 3
		if (v == Type.IMAGE) {
			type = Type.INT;
			lValue.setType(type);
			return type;
		}
		throw new TypeCheckException("visitLValue");
	}

	/*
	 * AssignmentStatement ::= LValuelValue  Expre  
		symbolTable.enterScope() 
		visit children to check condition 
		Condition:  AssignmentCompatible (LValue.type, Expr.type) 
		symbolTable.leaveScope()
	 */
	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
		System.out.println("visitAssignmentStatement");
		// TODO Auto-generated method stub.
		Type type;
		st.enterScope();
		assignmentStatement.getlValue().visit(this, arg);
		Type l = assignmentStatement.getlValue().getType();
		assignmentStatement.getE().visit(this, arg);
		Type e = assignmentStatement.getE().getType();
		if (e == l) {
			st.leaveScope();
			return assignmentStatement;
		}
		else if (l == Type.PIXEL) {
			if (e == Type.INT) {
				st.leaveScope();
				return assignmentStatement;
			}
		}
		else if (l == Type.IMAGE) {
			if (e == Type.PIXEL || e == Type.INT || e == Type.STRING) {
				st.leaveScope();
				return assignmentStatement;
			}
		}
		st.leaveScope();
		throw new TypeCheckException("visitAssignmentStatement");
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
		System.out.println("visitWriteStatement");
		writeStatement.getExpr().visit(this, arg);
		return writeStatement;
	}

	@Override
	public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
		System.out.println("visitDoStatement");
		// TODO Auto-generated method stub
		throw new TypeCheckException("visitDoStatement");
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
		System.out.println("visitIfStatement");
		// TODO Auto-generated method stub
		throw new TypeCheckException("visitIfStatement");
	}

	@Override
	public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
		System.out.println("visitGuardedBlock");
		// TODO Auto-generated method stub
		throw new TypeCheckException("visitGuardedBlock");
	}

	/*
	 * ReturnStatement ::= Expre 
            Condition:  Expr.type == Program.type  (where Program is the enclosing program)
	 */
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
		System.out.println("visitReturnStatement");
		Type type = (Type) returnStatement.getE().visit(this, arg);
		check(type == root.getType(), returnStatement, "image red must be int");
		return type;
	}

	@Override
	public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
		System.out.println("visitBlockStatement");
		// TODO Auto-generated method stub
		throw new TypeCheckException("visitBlockStatement");
	}

}

