package com.uepb;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    public record TableInput(String symbolName, String valor){};

    private final Map<String, TableInput> tables;

    public SymbolTable() {
        tables = new HashMap<>();
    }

    public void insert(String symbolName, String valor) {
        tables.put(symbolName, new TableInput(symbolName, valor));
    }

    public TableInput check(String symbolName) {
        return tables.get(symbolName);
    }
}
