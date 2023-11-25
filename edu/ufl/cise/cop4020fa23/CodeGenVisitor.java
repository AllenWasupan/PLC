package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.awt.Color;

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
import edu.ufl.cise.cop4020fa23.exceptions.CodeGenException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;

public class CodeGenVisitor implements ASTVisitor {
    String imp;
	//String StringBuilder;
    HashMap<String, Stack<String>> map;

    CodeGenVisitor() {
		System.out.println("CodeGenVisitor");
        map = new HashMap<>();
	}
    String type(String t) {
        t = t.toLowerCase();
        if (t.equals("string")) {
            t = "String";
        }
        if (t.equals("image")) {
            t = "BufferedImage";
        }
        if (t.equals("pixel")) {
            t = "int";
        }
        if (t.equals("boolean")) {
            t = "Boolean";
        }
        return t;
    }
    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        System.out.println("visitProgramGen");
        String s;
        String p;
        String t = program.getType().name();
        t = type(t);

        String b = (String) program.getBlock().visit(this, arg);

        p = "";
        for (int i = 0; i < program.getParams().size(); i++) {
            String para;
            if (i > 0) {
                p += ",";
            }
            para = program.getType().toString().toLowerCase() + " " + (String) program.getParams().get(i).visit(this, arg);
            para = type(para);
            
            p +=  para;
            
            System.out.println("ayp " + para);
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
        System.out.println(map);
        return s;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        System.out.println("visitBlockGen");
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
        System.out.println("visitNameDefGen");
        
        String t = nameDef.getType().name();
        t = type(t);
        String s;
        s =  nameDef.getIdentToken().text();
        
        System.out.println("t " + t);
        System.out.println("s " + s);
        String n;
        if (map.get(s) == null) {
            Stack<String> narr;
            if (map.get(s) == null) {
                n = s + "$0";
                narr = new Stack<String>();
            }
            else {
                n = s+"$"+map.get(s).size();
                narr = map.get(s);
            }
            narr.push(n);
            map.put(s,narr);
            n = t + " " + n;
        }
        else {
            n = map.get(s).peek();
            System.out.println("visitIdentExprG " + n);
        }

        System.out.println("namdef returning " + n);
        return n;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        System.out.println("visitDeclarationG");
        String s;
        String n = (String) declaration.getNameDef().visit(this, arg);
        System.out.println("visitDeclarationG n " + n);
        if (declaration.getInitializer() != null) {
            String e = (String) declaration.getInitializer().visit(this, arg);
            System.out.println("visitDeclarationG e " + e);
            s = n + "=" + e + ";\r\n";
            System.out.println("visitDeclarationG s " + s);
            return s;
        }
        s = n + ";\r\n";
        return s;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitStringLitExprG");
        String s;
        s = stringLitExpr.getText();
        System.out.println(s);
        return s;
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
        s = identExpr.getName();
        
        String n;
        if (map.get(s) == null) {
            Stack<String> narr;
            if (map.get(s) == null) {
                n = s + "$0";
                narr = new Stack<String>();
            }
            else {
                n = s+"$"+map.get(s).size();
                narr = map.get(s);
            }
            narr.push(n);
            map.put(s,narr);
        }
        else {
            System.out.println("visitIdentExprG " + map.get(s));
            n = map.get(s).peek();
        }
        
        System.out.println("Ident Returning " + n);
        return n;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitConditionalExprG");
        String s;
        String g = (String) conditionalExpr.getGuardExpr().visit(this, arg);
        String t = (String) conditionalExpr.getTrueExpr().visit(this, arg);
        String f = (String) conditionalExpr.getFalseExpr().visit(this, arg);
        s = "(" + g + "?" + t + ":" + f + ")" + ";\r\n";
        System.out.println(s);
        return s;
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
            s = "((int)Math.round(Math.pow(" + left + "," + right + ")))";
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
        String op;
        if (o == Kind.MINUS) {
            op = "-";
        }
        else if (o == Kind.BANG) {
            op = "!";
        }
        else {
            throw new CodeGenException("visitUnaryExprG");
        }
        s = "(" + op + e + ")";
        return s;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        System.out.println("visitLValueGen");
        String s;
        s = (String) lValue.getNameDef().visit(this, arg);
        return s;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        System.out.println("visitAssignmentStatementGen");
        String s;
        String l = (String) assignmentStatement.getlValue().visit(this, arg);
        String e = (String) assignmentStatement.getE().visit(this, arg);
        s = l + "=" + e + ";";
        return s;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        
        System.out.println("visitWriteStatementG");
        String s;
        s = "ConsoleIO.write(" + writeStatement.getExpr().visit(this, arg) + ");\r\n";
        System.out.println(s);
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
        System.out.println("visitBlockStatementGen");
        String s;
        s = (String) statementBlock.getBlock().visit(this, arg) + ";";
        return s;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitBooleanLitExprGen");
        String s;
        s = (String) booleanLitExpr.getText();
        return s;
    }

    //Ignore until assignment 5

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitConstExprGen");
        String n = constExpr.getName();
        System.out.println("n" + n + ".");
        int c;
        if (n.equals("Z")) {
            return "255";
        }
        else {
            
            switch (n.toLowerCase()) {
                case "red":
                    c = Color.RED.getRGB();
                    break;
                case "blue":
                    c = Color.BLUE.getRGB();
                    break;
                case "green":
                    c = Color.GREEN.getRGB();
                    break;
                case "gray":
                    c = Color.GRAY.getRGB();
                    break;
                case "pink":
                    c = Color.PINK.getRGB();
                    break;
                case "black":
                    c = Color.BLACK.getRGB();
                    break;
                case "cyan":
                    c = Color.CYAN.getRGB();
                    break;
                case "dark_gray":
                    c = Color.DARK_GRAY.getRGB();
                    break;
                case "light_gray":
                    c = Color.lightGray.getRGB();
                    break;
                case "magenta":
                    c = Color.MAGENTA.getRGB();
                    break;
                case "white":
                    c = Color.WHITE.getRGB();
                    break;
                default:
                    throw new CodeGenException();
            }
            
            
            return "0x" +
            Integer.toHexString(c);
        }
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
    System.out.println("visitPostfixExprGen");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitPostfixExpr'");
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
    System.out.println("visitChannelSelectorGen");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitChannelSelector'");
    }
    
    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
    System.out.println("visitPixelSelectorGen");
        String s = "";
        s += pixelSelector.xExpr().visit(this, arg);
        s += ",";
        s += pixelSelector.yExpr().visit(this, arg);
        return s;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitExpandedPixelExprGen");
        String s = "PixelOps.pack(";
        System.out.println("s " + s);
        s += expandedPixelExpr.getRed().visit(this, arg).toString();
        System.out.println("s " + s);
        s += ",";
        s += expandedPixelExpr.getGreen().visit(this, arg);
        s += ",";
        s += expandedPixelExpr.getBlue().visit(this, arg);
        s += ")";
        return s;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
    System.out.println("visitDimensionGen");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDimension'");
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
    System.out.println("visitDoStatementGen");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDoStatement'");
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
    System.out.println("visitIfStatementGen");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStatement'");
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
    System.out.println("visitGuardedBlockGen");
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitGuardedBlock'");
    }

    
}
