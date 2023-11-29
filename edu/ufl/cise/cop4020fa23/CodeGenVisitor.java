package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.awt.Color;
import java.awt.image.BufferedImage;

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
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps.OP;

public class CodeGenVisitor implements ASTVisitor {
    String imp;
	//String StringBuilder;
    HashMap<String, Set<String>> map;
    HashMap<String, String> valmap;
    CodeGenVisitor() {
		System.out.println("CodeGenVisitor");
        map = new HashMap<>();
        valmap = new HashMap<>();
	}

    String type(String t) {
        
        t = t.toLowerCase();
        System.out.println("Type " + t);
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
    String search(String s) {
        System.out.println("searching for " + s);
        String sol = null;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '$') {
                sol = s.substring(0, i);
                System.out.println("found " + sol);
            }
        }
        
        return sol;
    }
    int colint(String s) {
        System.out.println("colint");
        s = s.substring(2,10);
        int red = Integer.parseInt(s.substring(0, 2), 16);
        int green = Integer.parseInt(s.substring(2, 4), 16);
        int blue = Integer.parseInt(s.substring(4, 6), 16);
        int alpha = Integer.parseInt(s.substring(6, 8), 16);
        // Print the values (or use them as needed)
        System.out.println("Red: " + red);
        System.out.println("Green: " + green);
        System.out.println("Blue: " + blue);
        System.out.println("Alpha: " + alpha);
        return 0;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        System.out.println("A\nA\nA\nvisitProgramGen");
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
            para = program.getType().toString().toLowerCase();
            para = type(para);
            System.out.println("errrmmm " + program.getParams().get(i));
            para += " " + (String) program.getParams().get(i).visit(this, arg);
            p +=  para;
            
            System.out.println("ayp " + para);
        }

        String body = "";
        body += "public class " + program.getName() + "{\r\npublic static " + 
        t + " apply(" + p + ")\r\n" + 
        b + "}";
        String head;
        System.out.println("Body\n" + body);
        head = "package edu.ufl.cise.cop4020fa23;\r\n";
        if (body.contains("write")) {
            head += "import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;\r\n";
        }
        if (body.contains("BufferedImage")) {
            head += "import java.awt.image.BufferedImage;\r\n";
            if (head.contains("import edu.ufl.cise.cop4020fa23.runtime.ImageOps;") == false) {
                head += "import edu.ufl.cise.cop4020fa23.runtime.ImageOps;";
            }
        }
        if (body.contains("FileURLIO")) {
            head += "import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;\r\n";
        }
        if (body.contains("ImageOps")) {
            head += "import edu.ufl.cise.cop4020fa23.runtime.ImageOps;\r\n";
        }
        if (body.contains("PixelOps")) {
            head += "import edu.ufl.cise.cop4020fa23.runtime.PixelOps;\r\n";
        }

        s = head + body;
        System.out.println("Map " + map + "\n");
        return s;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        System.out.println("\nvisitBlockGen");
        String s;
        String e = "";
        List<BlockElem> elems = block.getElems();
        for (int i = 0; i < elems.size(); i++) {
            e = e + elems.get(i).visit(this,arg);
            System.out.println("ayb " + e);
        }
        System.out.println("BLOCKBLOCK " + e + "\nA\n");
        s = "{" + e + "}";
        return s;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        System.out.println("visitNameDefGen" + nameDef);
        
        String t = nameDef.getType().name();
        t = type(t);
        String s;
        s =  nameDef.getIdentToken().text();
        if (nameDef.getDimension() != null) {
            nameDef.getDimension().visit(this, arg);
        }
        
        System.out.println("t " + t);
        System.out.println("s " + s);
        String n = null;
        if (map.get(s) == null) {
            Set<String> narr;
            if (map.get(s) == null) {
                n = s + "$0";
                narr = new HashSet();
            }
            else {
                n = s+"$"+map.get(s).size();
                narr = map.get(s);
            }
            narr.add(n);
            map.put(s,narr);
            n = t + " " + n;
        }
        else {
            Iterator<String> iterator = map.get(s).iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                System.out.println(name);
                n = name;
            }
            System.out.println("namedef " + n + " " + s);
        }

        System.out.println("namdef returning " + n);
        return n;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        System.out.println("visitDeclarationG");
        String s;
        String n = (String) declaration.getNameDef().visit(this, arg);
        String name;
        if (n.contains(" ")) {
            String[] typename = n.split(" ");
            name = typename[1];
        }
        else {
            name = n;
        }
        System.out.println("visitDeclarationG n " + n);

        
        //Old
        if (declaration.getInitializer() != null) {
            String e = (String) declaration.getInitializer().visit(this, arg);
            System.out.println("visitDeclarationG e " + e);
            valmap.put(name,e);
            s = n + "=" + e + ";\r\n";
            System.out.println("visitDeclarationG s " + s);
            return s;
        }
        if (declaration.getNameDef().getType() != Type.IMAGE) {
            s = n + ";\r\n";
            return s;
        }
        
        else {
            if (declaration.getInitializer().getType() == Type.STRING) {
                if (declaration.getNameDef().getDimension() != null) {
                    return FileURLIO.readImage((String)declaration.getInitializer().visit(this, arg),(int) declaration.getNameDef().getDimension().getWidth().visit(this, arg), (int) declaration.getNameDef().getDimension().getHeight().visit(this, arg));
                }
                return FileURLIO.readImage((String)declaration.getInitializer().visit(this, arg));
            }
            if (declaration.getInitializer().getType() == Type.IMAGE) {
                if (declaration.getNameDef().getDimension() == null) {
                    return ImageOps.cloneImage((BufferedImage) declaration.getInitializer().visit(this, arg));
                }
                else {
                    declaration.getNameDef().getDimension().visit(this, arg);
                    return ImageOps.copyAndResize((BufferedImage) declaration.getInitializer().visit(this, arg),(int) declaration.getNameDef().getDimension().getWidth().visit(this, arg),(int) declaration.getNameDef().getDimension().getHeight().visit(this, arg));
                }
            }
            throw new CodeGenException("visitdeclg");
        }
        
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
            Set<String> narr;
            if (map.get(s) == null) {
                n = s + "$0";
                narr = new HashSet();
            }
            else {
                n = s+"$"+map.get(s).size();
                narr = map.get(s);
            }
            narr.add(n);
            map.put(s,narr);
        }
        else {
            System.out.println("visitIdentExprG " + map.get(s));
            n = null;
            Iterator<String> iterator = map.get(s).iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                System.out.println(name);
                n = name;
            }
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
        
        System.out.println("Left " + l + " Right " + r + " op " + o);
        if (l == Type.STRING && o == Kind.EQ) {
            s = left + ".equals(" + right + ")";
        }
        else if (o == Kind.EXP) {
            s = "((int)Math.round(Math.pow(" + left + "," + right + ")))";
        }
        else {
            s = "(" + left + op + right + ")";
        }
        
        if (l == Type.PIXEL && r == Type.PIXEL) {
            if (left.contains("PixelOps.pack") == false && right.contains("PixelOps.pack") == false) {
                if (o == Kind.PLUS) {
                    System.out.println("pixpix ");
                    System.out.println("onto ints " + left + " " + right + " " + binaryExpr);
                    if (left.contains("$")) {
                        System.out.println("$ ");
                        Iterator<String> iterator = map.get(search(left)).iterator();
                        while (iterator.hasNext()) {
                            String name = iterator.next();
                            System.out.println(name);
                            left = name;
                        }
                        iterator = map.get(search(right)).iterator();
                        while (iterator.hasNext()) {
                            String name = iterator.next();
                            System.out.println(name);
                            right = name;
                        }
                        left = valmap.get(left);
                        right = valmap.get(right);
                    }
                    System.out.println(left + " " + right);
                    int lpint = (int) Long.parseLong(left.substring(2, left.length()), 16);
                    int rpint = (int) Long.parseLong(right.substring(2, right.length()), 16);
                    System.out.println("now adding " + lpint + " " + rpint);
                    System.out.println();
                    int lprp = ImageOps.binaryPackedPixelPixelOp(OP.PLUS, lpint, rpint);
                    System.out.println("lprp " + lprp);
                    return "0x" + Integer.toHexString(lprp);
                }
            }
            else {
                s = "ImageOps.binaryPackedPixelPixelOp(";
                if (o == Kind.PLUS) {
                    s += "ImageOps.OP.PLUS,";
                }
                s += left + "," + right + ")";
                
            }
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
        s = (String) booleanLitExpr.getText().toLowerCase();
        return s;
    }

    //Ignore until assignment 5

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        System.out.println("visitConstExprGen");
        String n = constExpr.getName();
        System.out.println("color is " + n + ".");
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
            
            System.out.println("return color is " + c + " " + Integer.toHexString(c) + " " );
            return "0x" + Integer.toHexString(c);
        }
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
    System.out.println("visitPostfixExprGen");
        // TODO Auto-generated method stub
        String s = null;
        Type t = postfixExpr.getType();
        String p = (String) postfixExpr.primary().visit(this, arg);
        if (t == Type.PIXEL) {
            String c = (String) postfixExpr.channel().visit(this, arg);
            s = c + p;
            return s;
        }
        else {
            if (postfixExpr.pixel() != null && postfixExpr.channel() == null) {
                s = "ImageOps.getRGB(" + p + "," + postfixExpr.pixel().visit(this, arg) + ")";
                return s;
            }
            else if (postfixExpr.pixel() != null && postfixExpr.channel() != null) {
                //Add invoke PixelOps r, g, then b
                int r = PixelOps.red(0);
                int g = PixelOps.green(0);
                int b = PixelOps.blue(0);
                String c = (String) postfixExpr.channel().visit(this, arg);
                String pix = (String) postfixExpr.pixel().visit(this, arg);
                System.out.println("channel " + c + " pix " + pix);
                s = c + "(ImageOps.getRGB(" + p + "," + pix + "))";
            }
            else if (postfixExpr.pixel() == null && postfixExpr.channel() != null) {
                Kind c = (Kind) postfixExpr.channel().visit(this, arg);
                System.out.println("channel " + c);
                if (c == Kind.RES_red) {
                    s = "ImageOps.extractRed(" + p + ")";
                }
                if (c == Kind.RES_green) {
                    s = "ImageOps.extractGreen(" + p + ")";
                }
                if (c == Kind.RES_blue) {
                    s = "ImageOps.extractBlue(" + p + ")";
                }
                
                
            }
        }
        return s;
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        System.out.println("visitChannelSelectorGen");
        System.out.println("color " + channelSelector.color());
        return channelSelector.color();
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
        String s;
        s = (String) dimension.getWidth().visit(this, arg);
        s += "," + (String) dimension.getHeight().visit(this, arg);
        return s;
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
