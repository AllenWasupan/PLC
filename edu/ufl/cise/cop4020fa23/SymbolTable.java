package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.Stack;
import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

public class SymbolTable {
    
    int current_num;//serial number of current scope
    int next_num; //next serial number to assign

    private Stack<Integer> scope_stack;
    HashMap<String, Entry> symbolTable;


    SymbolTable() {
        scope_stack = new Stack<>();
        symbolTable = new HashMap<>();
        current_num = 0;
        next_num = 1;
    }

    void enterScope() {
        System.out.println("enterScope");
        current_num = next_num++;
        scope_stack.push(current_num);
    }
    
    void leaveScope() {
        System.out.println("leaveScope");
        current_num = scope_stack.pop();
    }

    NameDef lookup(String name) throws TypeCheckException {
        System.out.println("lookup");
        System.out.println("looking for " + name);
        System.out.println("st " + symbolTable.keySet());
        System.out.println("stack " + scope_stack);
        //lookup entry with key "name" in symbol table
        Entry entry = symbolTable.get(name);
        if (entry == null) {
            throw new TypeCheckException("Lookup entry null");
        }
        
        //scan chain-entries whose serial number is in the scope stack are visible
        int count = 999;
        NameDef n = null;
        if (entry.getNameDef() != null && entry.getEntry() == null) {
            n = entry.getNameDef();
            System.out.println("lu1 returning " + n);
            return n;
        }

        while (entry.getEntry() != null) {
            System.out.println("Entry " + entry.getScope() + " " + entry.getNameDef() + " " + entry.getEntry());
            System.out.println(current_num);
            //Return entry with with serial number closest to the top of the scopestack
            if (current_num - entry.getScope() < count && count > 0) {
                count = current_num - entry.getScope();
                n = entry.getNameDef();
            }
            entry = entry.getEntry();
        }
        
        if (n != null) {
            System.out.println("lu2 returning " + n);
            return n;
        }
        
        //If none, this is an error - the name is not bound in the current scope.    
        System.out.println("Failed");
        throw new TypeCheckException("Lookup");
        
    }

    void insert(NameDef nameDef) {
        System.out.println("Inserting " + nameDef);
        if (symbolTable.get(nameDef.getName()) == null) {
            Entry e = new Entry(nameDef,current_num);
            symbolTable.put(nameDef.getName(), e);
        }
        else {
            Entry e = new Entry(nameDef, current_num, symbolTable.get(nameDef.getName()));
            symbolTable.put(nameDef.getName(), e);
        }
    }

}

class Entry {
    NameDef nameDef;
    Integer scope_id;
    Entry entry;

    Entry(NameDef nameDef, int num) {
        this.nameDef = nameDef;
        this.scope_id = num;
        this.entry = null;
    }
    Entry(NameDef nameDef, int num,Entry entry) {
        this.nameDef = nameDef;
        this.scope_id = num;
        this.entry = entry;
    }

    Entry getEntry() {
        return this.entry;
    }

    int getScope() {
        return this.scope_id;
    }

    NameDef getNameDef() {
        return this.nameDef;
    }
}