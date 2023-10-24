package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    
    int current_num;//serial number of current scope
    int next_num; //next serial number to assign
    private Stack<Integer> scope_stack;
    Map<String, Integer> symbolTable;


    SymbolTable() {
        current_num = 0;
        next_num = 1;
        scope_stack = new Stack<>();
        symbolTable = new HashMap<>();
    }

    void lookup(String name) {
        Integer closestSerialNumber = null;

        for (Integer serialNumber : symbolTable.values()) {
            if (scope_stack.contains(serialNumber) && (closestSerialNumber == null || scope_stack.indexOf(serialNumber) > scope_stack.indexOf(closestSerialNumber))) {
                closestSerialNumber = serialNumber;
            }
        }

        if (closestSerialNumber != null) {
            System.out.println("Found entry for " + name + " with serial number: " + closestSerialNumber);
        } else {
            System.out.println("Error: The name " + name + " is not bound in the current scope.");
        }
    }

    void insert() {

    }

    void enterScope() {
        current_num = next_num++;
        scope_stack.push(current_num);
    }
    
    void leaveScope() {
        
        current_num = scope_stack.pop();
    }
}