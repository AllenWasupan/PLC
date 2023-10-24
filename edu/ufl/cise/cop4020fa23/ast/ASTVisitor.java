/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */
package edu.ufl.cise.cop4020fa23.ast;

import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

public interface ASTVisitor {


	/**
	 * @param assignmentStatement
	 * @param arg
	 * @return
	 */
	Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException;

	/**
	 * @param binaryExpr
	 * @param arg
	 * @return
	 */
	Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException;

	/**
	 * @param block
	 * @param arg
	 * @return
	 */
	Object visitBlock(Block block, Object arg) throws PLCCompilerException;

	/**
	 * @param statementBlock
	 * @param arg
	 * @return
	 */
	Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException;

	/**
	 * @param channelSelector
	 * @param arg
	 * @return
	 */
	Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException;

	/**
	 * @param conditionalExpr
	 * @param arg
	 * @return
	 */
	Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException;

	/**
	 * @param declaration
	 * @param arg
	 * @return
	 */
	Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException;

	/**
	 * @param dimension
	 * @param arg
	 * @return
	 */
	Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException;

	/**
	 * @param doStatement
	 * @param arg
	 * @return
	 */
	Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException;

	/**
	 * @param expandedPixelExpr
	 * @param arg
	 * @return
	 */
	Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException;

	/**
	 * @param guardedBlock
	 * @param arg
	 * @return
	 */
	Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException;

	/**
	 * @param identExpr
	 * @param arg
	 * @return
	 */
	Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException;

	/**
	 * @param ifStatement
	 * @param arg
	 * @return
	 */
	Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException;

	/**
	 * @param lValue
	 * @param arg
	 * @return
	 */
	Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException;

	/**
	 * @param nameDef
	 * @param arg
	 * @return
	 */
	Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException;

	/**
	 * @param numLitExpr
	 * @param arg
	 * @return
	 */
	Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException;

	/**
	 * @param pixelSelector
	 * @param arg
	 */
	Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException;

	/**
	 * 
	 * @param postfixExpr
	 * @param arg
	 * @return
	 * @throws PLCCompilerException
	 */
	Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException;

	/**
	 * @param program
	 * @param arg
	 * @return
	 * @throws PLCCompilerException 
	 */
	Object visitProgram(Program program, Object arg) throws PLCCompilerException;

	/**
	 * @param returnStatement
	 * @param object
	 * @return
	 */
	Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException;

	/**
	 * @param stringLitExpr
	 * @param arg
	 * @return
	 */
	Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException;

	/**
	 * @param unaryExpr
	 * @param arg
	 * @return
	 */
	Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg)throws PLCCompilerException;

	/**
	 * @param writeStatement
	 * @param arg
	 * @return
	 */
	Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException;

	/**
	 * @param expandedPixelExpr
	 * @param arg
	 * @return
	 */

	Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException;

	/**
	 * @param loopAssignmentStatement
	 * @param arg
	 * @return
	 */

	Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException;



}

class Visitor implements ASTVisitor {

    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
		return assignmentStatement;
        
    }

	/**
	 * @param binaryExpr
	 * @param arg
	 * @return
	 */
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
		return binaryExpr;
    }

	/**
	 * @param block
	 * @param arg
	 * @return
	 */
	public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
		return block;
    }


	/**
	 * @param statementBlock
	 * @param arg
	 * @return
	 */
	public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException{
        return statementBlock;
    }

	/**
	 * @param channelSelector
	 * @param arg
	 * @return
	 */
	public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException{
        return channelSelector;
    }

	/**
	 * @param conditionalExpr
	 * @param arg
	 * @return
	 */
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException{
        return conditionalExpr;
    }

	/**
	 * @param declaration
	 * @param arg
	 * @return
	 */
	public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException{
        return declaration;
    }

	/**
	 * @param dimension
	 * @param arg
	 * @return
	 */
	public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        return dimension;
    }

	/**
	 * @param doStatement
	 * @param arg
	 * @return
	 */
	public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException{
        return doStatement;
    }

	/**
	 * @param expandedPixelExpr
	 * @param arg
	 * @return
	 */
	public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException{
        return expandedPixelExpr;
    }

	/**
	 * @param guardedBlock
	 * @param arg
	 * @return
	 */
	public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException{
        return guardedBlock;
    }

	/**
	 * @param identExpr
	 * @param arg
	 * @return
	 */
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException{
        return identExpr;
    }

	/**
	 * @param ifStatement
	 * @param arg
	 * @return
	 */
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException{
        return ifStatement;
    }

	/**
	 * @param lValue
	 * @param arg
	 * @return
	 */
	public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException{
        return lValue;
    }

	/**
	 * @param nameDef
	 * @param arg
	 * @return
	 */
	public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException{
        return nameDef;
    }

	/**
	 * @param numLitExpr
	 * @param arg
	 * @return
	 */
	public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException{
        return numLitExpr;
    }

	/**
	 * @param pixelSelector
	 * @param arg
	 */
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException{
        return pixelSelector;
    }

	/**
	 * 
	 * @param postfixExpr
	 * @param arg
	 * @return
	 * @throws PLCCompilerException
	 */
	public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException{
        return postfixExpr;
    }

	/**
	 * @param program
	 * @param arg
	 * @return
	 * @throws PLCCompilerException 
	 */
	public Object visitProgram(Program program, Object arg) throws PLCCompilerException{
        return program;
    }

	/**
	 * @param returnStatement
	 * @param object
	 * @return
	 */
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException{
        return returnStatement;
    }

	/**
	 * @param stringLitExpr
	 * @param arg
	 * @return
	 */
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException{
        return stringLitExpr;
    }

	/**
	 * @param unaryExpr
	 * @param arg
	 * @return
	 */
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg)throws PLCCompilerException{
        return unaryExpr;
    }

	/**
	 * @param writeStatement
	 * @param arg
	 * @return
	 */
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException{
        return writeStatement;
    }

	/**
	 * @param expandedPixelExpr
	 * @param arg
	 * @return
	 */

	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException{
        return booleanLitExpr;
    }

	/**
	 * @param loopAssignmentStatement
	 * @param arg
	 * @return
	 */

	public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException{
        return constExpr;
    }
}