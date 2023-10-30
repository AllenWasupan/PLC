package edu.ufl.cise.cop4020fa23;

import static org.junit.jupiter.api.Assertions.fail;

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
import edu.ufl.cise.cop4020fa23.ast.SyntheticNameDef;
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
			throw new TypeCheckException(string);
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
			System.out.println("Block elem " + elem);
		}
		st.leaveScope();
		System.out.println("leaving vblock");
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
			if (type == Type.IMAGE) {
				d.visit(this, arg);

				nameDef.setType(type);				
				st.insert(nameDef);
				return nameDef;
			}
		}
		else if (type == Type.INT || type == Type.BOOLEAN || type == Type.STRING || type == Type.PIXEL || type == Type.IMAGE) {
			
			nameDef.setType(type);
			st.insert(nameDef);
			return nameDef;
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
		System.out.println("\nvisitDeclaration");
		System.out.println(declaration);
		Type type;
		
		Expr e = declaration.getInitializer();
		
		System.out.println("visitdec E " + e);
		if (e != null) {
			System.out.println(e.getType());
			e.visit(this, arg);
			System.out.println("E " + e);
			System.out.println("Type " + e.getType());
		}

		NameDef n = declaration.getNameDef();
		System.out.println("N " + n);
		n.visit(this, arg);
		
		
		if (e == null || e.getType() == n.getType() || (e.getType() ==  Type.STRING && n.getType() == Type.IMAGE)) {
			
			type = n.getType();
			
			n.setType(type);
			System.out.println("leaving visitdec");
			System.out.println(declaration);
			
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
		Type type;
		Expr g = conditionalExpr.getGuardExpr();
		g.visit(this, arg);
		Expr t = conditionalExpr.getTrueExpr();
		t.visit(this, arg);
		Expr f = conditionalExpr.getFalseExpr();
		f.visit(this, arg);
		check(g.getType() == Type.BOOLEAN, conditionalExpr, "GuardExpr must be bool");
		check(t.getType() == f.getType(), conditionalExpr, "t = f");
		type = t.getType();
		conditionalExpr.setType(type);
		return type;
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
			return infertype;
		}
		
		throw new TypeCheckException("Unimplemented Method ASTVisitor.visitBinaryExpr invoked.");
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
		return type;
	}

	/*
	 * Condition:  inferPostfixExprType is defined 
		PostfixExpr.type  inferPostfixExprType(Epxr.type, PixelSelector, ChannelSelector)
	 */
	@Override
	public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
		System.out.println("\nvisitPostfixExpr");
		System.out.println(postfixExpr);
		
		Type type;
		postfixExpr.primary().visit(this, arg);
		Type e = postfixExpr.primary().getType();
		System.out.println("e " + e);
		PixelSelector p = postfixExpr.pixel();
		if (p != null) {
			p.visit(this, arg);
		}
		
		System.out.println("p " + p);
		ChannelSelector c = postfixExpr.channel();
		if (c != null) {
			c.visit(this, arg);
		}
		
		System.out.println("c " + c);

		if (p == null && c == null) {
			type = e;
			postfixExpr.setType(type);
			return type;
		}
		if (e == Type.IMAGE) {
			if (p != null) {
				if (c == null) {
					type = Type.PIXEL;
					postfixExpr.setType(type);
					return type;
				}
				else {
					type = Type.INT;
					postfixExpr.setType(type);
					return type;
				}
			}
			else {
				if (c != null) {
					type = Type.IMAGE;
					postfixExpr.setType(type);
					return type;
				}
			}
		}
		else if (e == Type.PIXEL) {
			if (p == null && c != null) {
				type = Type.INT;
				postfixExpr.setType(type);
				return type;
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
		System.out.println("\nvisitIdentExpr");
		System.out.println(identExpr);
		NameDef n = st.lookup(identExpr.getName());
		
		Type type;
		if (n != null) {
			System.out.println("leaving visitident " + n);
			identExpr.setNameDef(n);
			type = n.getType();
			identExpr.setType(type);
			System.out.println("Type " + identExpr.getType());
			return type;
		}
		throw new TypeCheckException("visitIdentExpr");
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

	/*
	 * ChannelSelector ::= red |  green |  blue
	 */
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
		System.out.println("\nvisitPixelSelector");
		System.out.println(arg);
		Type type;
		Expr x = pixelSelector.xExpr();
		Expr y = pixelSelector.yExpr();
		/* 
		if (arg == "LValue") {

			System.out.println("x " + pixelSelector.xExpr());
			System.out.println("y " + pixelSelector.yExpr());
			st.enterScope();
			NameDef xn = new SyntheticNameDef("x");
			st.insert(xn);
			NameDef yn = new SyntheticNameDef("y");
			st.insert(yn);
			
			x.visit(this, arg);
			y.visit(this, arg);

			throw new TypeCheckException("'visitPixelSelector'");
		}*/
		
		x.visit(this, arg);
		y.visit(this, arg);
		type = Type.INT;
		check(x.getType() == Type.INT,x,"oof");
		check(y.getType() == Type.INT,y,"oof");
		
		return pixelSelector;
		//throw new TypeCheckException("'visitPixelSelector'");
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
		System.out.println("\nvisitExpandedPixelExpr");
		Expr r = expandedPixelExpr.getRed();
		r.visit(this, arg);
		Expr g = expandedPixelExpr.getGreen();
		g.visit(this, arg);
		Expr b = expandedPixelExpr.getBlue();
		b.visit(this, arg);
		check(r.getType() == Type.INT,r,"oof");
		check(g.getType() == Type.INT,g,"oof");
		check(b.getType() == Type.INT,b,"oof");
		Type type = Type.INT;
		expandedPixelExpr.setType(type);
		return expandedPixelExpr;
	}
	
	//Dimension  ::=  Expr_width  Expr_height
	/*
	Condition: Exprwidth.type == INT
	Condition: Exprheight.type == INT
	 */
	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
		System.out.println("\nvisitDimension");
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
		System.out.println("\nvisitLValue");
		arg = "LValue";
		System.out.println(arg);
		
		Type type;

		PixelSelector p = lValue.getPixelSelector();
		if (p != null) {
			p.visit(this, arg);
			arg = null;
		}		
		ChannelSelector c = lValue.getChannelSelector();
		if (c != null) {
			c.visit(this, arg);
		}
		System.out.println(lValue);
		NameDef n = st.lookup(lValue.getName());
		lValue.setNameDef(n);
		Type v = lValue.getVarType();
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
			return lValue;
		}
		//Row 4 5
		if (p == null) {
			if (v == Type.IMAGE) {
				type = Type.IMAGE;
				lValue.setType(type);
				return lValue;
			}
			if (v == Type.PIXEL) {
				type = Type.INT;
				lValue.setType(type);
				return lValue;
			}
		}
		//Row 2
		if (c == null) {
			if (v == Type.IMAGE) {
				type = Type.PIXEL;
				lValue.setType(type);
				return lValue;
			}
			
		}
		//Row 3
		if (v == Type.IMAGE) {
			type = Type.INT;
			lValue.setType(type);
			return lValue;
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
		System.out.println("\nvisitAssignmentStatement");
		System.out.println(assignmentStatement);
		st.enterScope();
		LValue l = assignmentStatement.getlValue();
		l.visit(this, arg);
		Type ltype = assignmentStatement.getlValue().getType();
		
		Expr e = assignmentStatement.getE();
		e.visit(this, arg);
		Type etype = e.getType();
		System.out.println("as " + ltype);
		System.out.println("as " + etype);
		System.out.println("as " + e);
		if (etype == ltype) {
			st.leaveScope();
			return assignmentStatement;
		}
		else if (ltype == Type.PIXEL) {
			if (etype == Type.INT) {
				st.leaveScope();
				return assignmentStatement;
			}
		}
		else if (ltype == Type.IMAGE) {
			if (etype == Type.PIXEL || etype == Type.INT || etype == Type.STRING) {
				st.leaveScope();
				return assignmentStatement;
			}
		}
		System.out.println("Failed");
		throw new TypeCheckException("visitAssignmentStatement");
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
		System.out.println("\nvisitWriteStatement");
		writeStatement.getExpr().visit(this, arg);
		return writeStatement;
	}

	@Override
	public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
		System.out.println("visitDoStatement");
		System.out.println(doStatement);
		List<GuardedBlock> d = doStatement.getGuardedBlocks();
		for (int i = 0; i < d.size(); i++) {
			d.get(i).getGuard().visit(this, arg);
			System.out.println(d.get(i).getGuard());
			System.out.println(d.get(i).getGuard().getType());
			d.get(i).visit(this, arg);
			if (d.get(i).getGuard().getType() != Type.BOOLEAN) {
				throw new TypeCheckException("visitDoStatement");
			}
		}
		return doStatement;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
		System.out.println("visitIfStatement");
		List<GuardedBlock> g = ifStatement.getGuardedBlocks();
		for (int i = 0; i < g.size(); i++) {
			g.get(i).getGuard().visit(this, arg);
			if (g.get(i).getGuard().getType() != Type.BOOLEAN) {
				throw new TypeCheckException("visitIfStatement");
			}
		}
		return ifStatement;
	}

	@Override
	public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
		System.out.println("\nvisitGuardedBlock");
		Expr g = guardedBlock.getGuard();
		g.visit(this, arg);
		Block b = guardedBlock.getBlock();
		b.visit(this, arg);
		check(g.getType() == Type.BOOLEAN,guardedBlock,"oof");
		return guardedBlock;
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
		return returnStatement;
	}

	@Override
	public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
		System.out.println("visitBlockStatement");
		statementBlock.getBlock().visit(this, arg);
		return statementBlock;
		//throw new TypeCheckException("visitBlockStatement");
	}

}

