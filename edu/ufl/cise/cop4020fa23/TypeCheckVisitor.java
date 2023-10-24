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

public class TypeCheckVisitor implements ASTVisitor {
	SymbolTable st;
	Program root;

	TypeCheckVisitor() {
		st = new SymbolTable();
	}

	private void check(boolean b, Dimension dimension, String string) {

	}

	AST typeCheck(String input) throws PLCCompilerException {
		AST ast = ComponentFactory.makeParser(input).parse();
		ASTVisitor typeChecker = ComponentFactory.makeTypeChecker();
		ast.visit(typeChecker, null);
		return ast;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitAssignmentStatement invoked.");
	}
	
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented Method ASTVisitor.visitBinaryExpr invoked.");
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
		st.enterScope();
		List<BlockElem> blockElems = block.getElems();
		for (BlockElem elem : blockElems) {
			elem.visit(this, arg);
		}
		st.leaveScope();
		return block;
	}

	@Override
	public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitBlockStatement'");
	}

	@Override
	public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitChannelSelector'");
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitConditionalExpr'");
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitDeclaration'");
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
		Type typeW = (Type) dimension.getWidth().visit(this, arg);
		check(typeW == Type.INT, dimension, "image width must be int");
		Type typeH = (Type) dimension.getHeight().visit(this, arg);
		check(typeH == Type.INT, dimension, "image height must be int");
		return dimension;
	}


	@Override
	public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitDoStatement'");
	}

	@Override
	public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitExpandedPixelExpr'");
	}

	@Override
	public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitGuardedBlock'");
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitIdentExpr'");
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitIfStatement'");
	}

	@Override
	public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitLValue'");
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitNameDef'");
	}

	@Override
	public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
		Type type = Type.INT;
		numLitExpr.setType(type);
		return type;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitPixelSelector'");
	}

	@Override
	public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitPostfixExpr'");
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
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

	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitReturnStatement'");
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitStringLitExpr'");
	}

	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitUnaryExpr'");
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
		writeStatement.getExpr().visit(this, arg);
		return writeStatement;
	}

	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitBooleanLitExpr'");
	}

	@Override
	public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'visitConstExpr'");
	}

}

