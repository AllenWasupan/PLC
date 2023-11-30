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
    String head;
	//String StringBuilder;
    HashMap<String, HashMap<String,Type>> map;
    HashMap<String, String> valmap;
    CodeGenVisitor() {
		System.out.println("CodeGenVisitor");
        map = new HashMap<>();
        valmap = new HashMap<>();
        head = "package edu.ufl.cise.cop4020fa23;\r\n";
	}

    String type(String t) {
        
        t = t.toLowerCase();
        System.out.println("Type " + t);
        if (t.equals("string")) {
            t = "String";
        }
        if (t.equals("image")) {
            t = "BufferedImage";
            if (head.contains("import java.awt.image.BufferedImage;")== false) {
                head += "import java.awt.image.BufferedImage;\r\n";
            }
        }
        if (t.equals("pixel")) {
            t = "int";
            if (head.contains("import edu.ufl.cise.cop4020fa23.runtime.PixelOps;")== false) {
                head += "import edu.ufl.cise.cop4020fa23.runtime.PixelOps;\r\n";
            }
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
    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }
    
    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        System.out.println("A1\nA2\nA3\nvisitProgramGen");
        String s;
        String p;
        String t = program.getType().name();
        t = type(t);

        String b = (String) program.getBlock().visit(this, arg);

        String e = "";
        List<BlockElem> elems = program.getBlock().getElems();
        for (int i = 0; i < elems.size(); i++) {
            e += elems.get(i).getClass().getName();
            elems.get(i).visit(this, arg);
        }
        System.out.println("Program e's " + e);
        p = "";
        for (int i = 0; i < program.getParams().size(); i++) {
            String para;
            if (i > 0) {
                p += ",";
            }
            
            para = program.getType().toString().toLowerCase();
            para = type(para);
            para += " " + (String) program.getParams().get(i).visit(this, arg);
            p +=  para;
            
            System.out.println("ayp " + para);
        }

        String body = "";
        body += "public class " + program.getName() + "{\r\npublic static " + 
        t + " apply(" + p + ")\r\n" + 
        b + "}";

        

        s = head + body;
        System.out.println("Valmap " + valmap);
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
            System.out.println("adding to block " + e);
        }
        System.out.println("Block returning " + e + "\nA\n");
        s = "{" + e + "}";
        return s;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        System.out.println("visitNameDefGen " + nameDef);
        
        String t = nameDef.getType().name();
        t = type(t);
        String s;
        s =  nameDef.getIdentToken().text();
        if (nameDef.getDimension() != null) {
            nameDef.getDimension().visit(this, arg);
        }
        
        String n = null;
        if (map.get(s) == null) {
            HashMap<String,Type> narr;
            if (map.get(s) == null) {
                n = s + "$0";
                narr = new HashMap<>();
            }
            else {
                n = s+"$"+(map.get(s).size()-1);
                narr = map.get(s);
            }
            narr.put(n, nameDef.getType());
            map.put(s,narr);
            n = t + " " + n;
        }
        else {
            HashMap<String,Type> setmap = map.get(s);
            for (String key : setmap.keySet()) {
                Type value = setmap.get(key);
                System.out.println("Key: " + key + ", Value: " + value);
                if (value == nameDef.getType()) {
                    n = key;
                    break;
                }
            }
        }
        System.out.println("namedef " + n + " " + s);
        
        return n;
    
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        System.out.println("visitDeclarationG");
        String s = "";
        Object dec = declaration.getNameDef().visit(this, arg);
        String n = (String) dec;
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
            s = n + "=" + e;
            
        }

        if (declaration.getNameDef().getType() == Type.IMAGE) {
            if (declaration.getInitializer() == null) {
                if (declaration.getNameDef().getDimension() == null) {
                    throw new CodeGenException();
                }
                ImageOps.makeImage(Integer.parseInt((String)declaration.getNameDef().getDimension().getWidth().visit(this, arg)), Integer.parseInt((String)declaration.getNameDef().getDimension().getHeight().visit(this,arg)));
                return "final BufferedImage" + n + "=" + "ImageOps.makeImage(" + declaration.getNameDef().getDimension().visit(this, arg) + ");";
            }
            if (declaration.getInitializer().getType() == Type.STRING) {
                String e = (String) declaration.getInitializer().visit(this, arg);
                if (head.contains("import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;")== false) {
                    head += "import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;\r\n";
                }
                if (declaration.getNameDef().getDimension() != null) {
                    System.out.println("visitDeclarationG fileio h and w" );
                    
                    s = n + "=" +"FileURLIO.readImage(" + e + 
                    (String) declaration.getNameDef().getDimension().getWidth().visit(this, arg) +  
                    (String) declaration.getNameDef().getDimension().getHeight().visit(this, arg) + ");";
                    FileURLIO.readImage(e);
                    //ImageOps.copyInto(e);
                    return s;
                }
                FileURLIO.readImage(e);
                return n + "=" + "FileURLIO.readImage(" + e + ");";
            }
            if (declaration.getInitializer().getType() == Type.IMAGE) {
                if (head.contains("import edu.ufl.cise.cop4020fa23.runtime.ImageOps;") == false) {
                    head += "import edu.ufl.cise.cop4020fa23.runtime.ImageOps;\r\n";
                }
                if (declaration.getNameDef().getDimension() == null) {
                    System.out.println("visitDeclarationG clone");
                    ImageOps.cloneImage((BufferedImage)declaration.getInitializer().visit(this, arg));
                    return "ImageOps.cloneImage(" + declaration.getInitializer().visit(this, arg) + ");";
                }
                else {
                    System.out.println("visitDeclarationG resize");
                    declaration.getNameDef().getDimension().visit(this, arg);
                    ImageOps.copyAndResize((BufferedImage)declaration.getInitializer().visit(this, arg),Integer.parseInt((String)declaration.getNameDef().getDimension().getWidth().visit(this, arg)),Integer.parseInt((String)declaration.getNameDef().getDimension().getHeight().visit(this, arg)));

                    return "ImageOps.copyAndResize(" + declaration.getInitializer().visit(this, arg) + "," +
                     declaration.getNameDef().getDimension().getWidth().visit(this, arg)+","+
                     declaration.getNameDef().getDimension().getHeight().visit(this, arg)+"); ;";
                }
            }
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
        System.out.println("visitIdentExprG " + identExpr);
        String s;
        s = (String) identExpr.getNameDef().visit(this, arg);
        if (s.contains(" ")) {
            String[] typename = s.split(" ");
            s = typename[1];
        }
        System.out.println("identvsname " + identExpr.getName() + " " + s);
        /*
        if (map.get(s) == null) {
            HashMap<String,Type> narr;
            if (map.get(s) == null) {
                n = s + "$0";
                narr = new HashMap<>();
            }
            else {
                n = s+"$"+(map.get(s).size()-1);
                narr = map.get(s);
            }
            narr.put(n,identExpr.getType());
            map.put(s,narr);
        }
        else {
 
            System.out.println("visitIdentExprG " + map.get(s));
            
            HashMap<String,Type> setmap = map.get(s);
            n = s + "$" + (setmap.size()-1);
            setmap.put(n, identExpr.getType());
            map.put(s,setmap);
        }*/
        
        return s;
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
            if (head.contains("import edu.ufl.cise.cop4020fa23.runtime.PixelOps;") == false) {
                head += "import edu.ufl.cise.cop4020fa23.runtime.PixelOps;\r\n";
            }
            if (head.contains("import edu.ufl.cise.cop4020fa23.runtime.ImageOps;") == false) {
                head += "import edu.ufl.cise.cop4020fa23.runtime.ImageOps;\r\n";
            }
            if (left.contains("PixelOps.pack") == false && right.contains("PixelOps.pack") == false) {
                if (o == Kind.PLUS) {
                    System.out.println("pixpix " + map);
                    System.out.println("onto ints " + left.substring(0,left.length()-2) + " " + right + " " + binaryExpr);
                    if (left.contains("$")) {
                        System.out.println("$ ");
                        HashMap<String,Type> setmap = map.get(left.substring(0,left.length()-2));
                        for (String key : setmap.keySet()) {
                            Type value = setmap.get(key);
                            System.out.println("Key: " + key + ", Value: " + value);
                            if (value == l) {
                                left = key;
                                break;
                            }
                        }
                        setmap = map.get(right.substring(0,right.length()-2));
                        for (String key : setmap.keySet()) {
                            Type value = setmap.get(key);
                            System.out.println("Key: " + key + ", Value: " + value);
                            if (value == r) {
                                right = key;
                                break;
                            }
                        }
                        System.out.println(left + " " + right + ".");
                        System.out.println(valmap + " " + valmap.get("p0$0"));
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
        else if (o == Kind.RES_height) {
            return "(" + e + ".getHeight())";
        }
        else if (o == Kind.RES_width) {
            return "(" + e + ".getWidth())";
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
        if (lValue.getChannelSelector() != null) {
            lValue.getChannelSelector().visit(this, arg);
        }
        if (lValue.getPixelSelector() != null) {
            lValue.getPixelSelector().visit(this, arg);
        }
        return s;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        System.out.println("visitAssignmentStatementGen " + assignmentStatement);
        String s;
        String l = (String) assignmentStatement.getlValue().visit(this, arg);
        String e = (String) assignmentStatement.getE().visit(this, arg);
        System.out.println("visitAssignmentStatementGen " + l + " " + e);
        if (assignmentStatement.getlValue().getType() == Type.IMAGE) {
            
            if (assignmentStatement.getlValue().getPixelSelector() == null &&assignmentStatement.getlValue().getChannelSelector() == null) {
                if (assignmentStatement.getE().getType() == Type.IMAGE) {
                    s = "ImageOps.copyInto(" + l + "," + e + ");";
                    ImageOps.copyInto((BufferedImage)assignmentStatement.getlValue().visit(this, arg),(BufferedImage)assignmentStatement.getE().visit(this, arg));
                }
                if (assignmentStatement.getE().getType() == Type.PIXEL) {
                    s = "ImageOps.setAllPixels(" + l + "," + e + ");";
                    ImageOps.setAllPixels((BufferedImage)assignmentStatement.getlValue().visit(this, arg),(int)assignmentStatement.getE().visit(this, arg));
                }
                if (assignmentStatement.getE().getType() == Type.STRING) {
                    s = "FileURLIO.readImage(" + e + "," +
                    assignmentStatement.getlValue().getNameDef().getDimension().getWidth() + "," + 
                    assignmentStatement.getlValue().getNameDef().getDimension().getHeight() + ");" +
                    "ImageOps.copyInto(" + e + "," + l + ");";
                    BufferedImage x = FileURLIO.readImage(e.substring(0,e.length()-2));
                    System.out.println("x " + x);
                    ImageOps.copyInto(x,(BufferedImage)assignmentStatement.getlValue().visit(this, arg));
                }
            }
            else if (assignmentStatement.getlValue().getChannelSelector() != null) {
                throw new CodeGenException();
            }
            else if (assignmentStatement.getlValue().getPixelSelector() != null && assignmentStatement.getlValue().getChannelSelector() == null) {
                //TODO
                throw new CodeGenException("incomplete");
            }
            throw new CodeGenException();
        }
        else if (assignmentStatement.getlValue().getType() == Type.PIXEL && assignmentStatement.getlValue().getChannelSelector() != null) {

            Kind c = (Kind) assignmentStatement.getlValue().getChannelSelector().visit(this, arg);
            if (c == Kind.RES_red) {
                s = "PixelOps.setRed(" + l + "," + e;
            }
            if (c == Kind.RES_green) {
                s = "PixelOps.setRed(" + l + "," + e;
            }
            if (c == Kind.RES_blue) {
                s = "PixelOps.setRed(" + l + "," + e;
            }
            else {
                throw new CodeGenException();
            }
        }
        else {
            
            s = l + "=" + e + ";";
        }
        
        return s;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        
        System.out.println("visitWriteStatementG");
        String s;
        s = "ConsoleIO.write(" + writeStatement.getExpr().visit(this, arg) + ");\r\n";
        if (head.contains("import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;") == false) {
            head += "import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;\r\n";
        }
        
        System.out.println(s);
        return s;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        System.out.println("visitReturnStatementG");
        String s;
        
        s = "return " + returnStatement.getE().visit(this, arg) + ";";
        System.out.println("returning " + s);
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
        Object p = postfixExpr.primary().visit(this, arg);
        if (t == Type.PIXEL) {
            Object c = postfixExpr.channel().visit(this, arg);
            s = (String) c + p;
            return s;
        }
        else {
            if (head.contains("import edu.ufl.cise.cop4020fa23.runtime.ImageOps;") == false) {
                head += "import edu.ufl.cise.cop4020fa23.runtime.ImageOps;\r\n";
            }
            if (postfixExpr.pixel() != null && postfixExpr.channel() == null) {
                Object pix = postfixExpr.pixel().visit(this, arg);
                s = "ImageOps.getRGB(" + p + "," + postfixExpr.pixel().visit(this, arg) + ")";
                ImageOps.getRGB((BufferedImage)p,Integer.parseInt((String)postfixExpr.pixel().xExpr().visit(this, arg)),Integer.parseInt((String)postfixExpr.pixel().yExpr().visit(this, arg)));
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
                    System.out.println(p);
                    ImageOps.extractRed((BufferedImage)p);
                }
                if (c == Kind.RES_green) {
                    s = "ImageOps.extractGrn(" + p + ")";
                    ImageOps.extractGrn((BufferedImage)p);
                }
                if (c == Kind.RES_blue) {
                    s = "ImageOps.extractBlu(" + p + ")";
                    ImageOps.extractBlu((BufferedImage)p);
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
        if (head.contains("import edu.ufl.cise.cop4020fa23.runtime.PixelOps;")== false) {
            head += "import edu.ufl.cise.cop4020fa23.runtime.PixelOps;\r\n";
        }
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
        Object r = expandedPixelExpr.getRed().visit(this, arg);
        Object g = expandedPixelExpr.getGreen().visit(this, arg);
        Object b = expandedPixelExpr.getBlue().visit(this, arg);
        s += r.toString();
        System.out.println("rgb " + r + g + b);
        s += ",";
        s += g;
        s += ",";
        s += b;
        s += ")";
        System.out.println("s " + s + expandedPixelExpr);
        
        if (isInteger((String)r) == false || isInteger((String)g)== false  || isInteger((String)b)== false ) {
            return s;
        }
        PixelOps.pack(Integer.parseInt((String)r),Integer.parseInt((String)g), Integer.parseInt((String)b));
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
        System.out.println(doStatement);
        throw new UnsupportedOperationException("Unimplemented method 'visitDoStatement'");
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
    System.out.println("visitIfStatementGen");
        // TODO Auto-generated method stub
        System.out.println(ifStatement);
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStatement'");
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
    System.out.println("visitGuardedBlockGen");
        // TODO Auto-generated method stub
        System.out.println(guardedBlock);
        throw new UnsupportedOperationException("Unimplemented method 'visitGuardedBlock'");
    }

    
}
