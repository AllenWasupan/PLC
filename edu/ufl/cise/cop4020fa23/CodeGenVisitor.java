package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.List;

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
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;

public class CodeGenVisitor implements ASTVisitor {
    String imp;
	//String StringBuilder;
    HashMap<String, Integer> map;

    CodeGenVisitor() {
		System.out.println("CodeGenVisitor");
        HashMap<String, Integer> map = new HashMap<>();
	}

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        System.out.println("visitProgramGen");
        String s;
        String p;
        String t = program.getType().name();
        t = t.toLowerCase();
        String b = (String) program.getBlock().visit(this, arg);

        p = "";
        for (int i = 0; i < program.getParams().size(); i++) {
            if (i > 0) {
                p += ",";
            }
            p += program.getParams().get(i).visit(this, arg) + "$1";
            System.out.println("ayp " + program.getParams().get(i).getName());
        }

        s = "package edu.ufl.cise.cop4020fa23;\r\n";


        String e = "";
        List<BlockElem> elems = program.getBlock().getElems();
        for (int i = 0; i < elems.size(); i++) {
            e = elems.get(i).getClass().getName();
            System.out.println("e " + e);
            if (e == "edu.ufl.cise.cop4020fa23.ast.WriteStatement") {
            s += "import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;\r\n";
            break;
            }
        }
        
        s += "public class " + program.getName() + "{\r\npublic static " + 
        t + " apply(" + p + ")\r\n" + 
        b + "}";
        System.out.println("yo\n" + s + "\noy");
        
        return s;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        String s;
        String e = "";
        List<BlockElem> elems = block.getElems();
        for (int i = 0; i < elems.size(); i++) {
            e = e + elems.get(i).visit(this,arg);
            System.out.println("ayb " + e);
        }
        s = "{" + e + "}";
        return s;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        System.out.println("visitNumLitExprG");
        String t = nameDef.getType().name();
        t = t.toLowerCase();
        String s;
        s = t + " " + nameDef.getIdentToken().text();
        System.out.println(s);
        return s;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDeclaration'");
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitStringLitExpr'");
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitNumLitExprG");
        String s;
        s = numLitExpr.getText();
        System.out.println(s);
        return s;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitIdentExprG");
        String s;
        s = identExpr.getName() + "$1";
        System.out.println(s);
        return s;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConditionalExpr'");
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitBinaryExprG");
        String s;
        String left = (String) binaryExpr.getLeftExpr().visit(this, arg);
        Type l = binaryExpr.getLeftExpr().getType();
        Kind o = binaryExpr.getOp().kind();
        String right = (String) binaryExpr.getRightExpr().visit(this, arg);
        Type r = binaryExpr.getRightExpr().getType();
        String op = binaryExpr.getOp().text();
        if (l == Type.STRING && o == Kind.EQ) {
            s = left + ".equals(" + right + ")";
        }
        else if (o == Kind.EXP) {
            s = "((int)Math.round(Math.pow" + left + "," + right + "))";
        }
        else {
            s = "(" + left + op + right + ")";
        }
        System.out.println(s);
        return s;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitUnaryExprG");
        String s;
        String e = (String) unaryExpr.getExpr().visit(this, arg);
        Type l = unaryExpr.getType();
        Kind o = unaryExpr.getOp();
        String op = unaryExpr.getOp().name();
        s = "(" + op + e + ")";
        return s;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLValue'");
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg)
            throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignmentStatement'");
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        
        System.out.println("visitWriteStatementG");
        String s;
        s = "ConsoleIO.write(" + writeStatement.getExpr().visit(this,arg) + ");\r\n";
        System.out.println(s);
        arg = "write";
        return s;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        System.out.println("visitReturnStatementG");
        String s;
        returnStatement.getE().visit(this, arg);
        s = "return " + returnStatement.getE().visit(this, arg) + ";";
        System.out.println(s);
        return s;
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBlockStatement'");
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBooleanLitExpr'");
    }

    //Ignore until assignment 5

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConstExpr'");
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitPostfixExpr'");
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitChannelSelector'");
    }
    
    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitPixelSelector'");
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitExpandedPixelExpr'");
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDimension'");
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDoStatement'");
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStatement'");
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitGuardedBlock'");
    }

    
}
