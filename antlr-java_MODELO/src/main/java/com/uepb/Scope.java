package com.uepb;

import java.util.LinkedList;
import java.util.Optional;
import com.uepb.SymbolTable.TableInput;

public class Scope {

    private final LinkedList<SymbolTable> scopes;

    public Scope(LinkedList<SymbolTable> scopes) {
        this.scopes = scopes;
    }

    public void startScope(){
        scopes.push(new SymbolTable());
    }

    public void dropScope(){
        scopes.removeFirst();
    }

    public SymbolTable getCurrentScope(){
        return scopes.peek();
    }

    public Optional<TableInput> findFirstOccurrenceOf(String symbolName) {
        return scopes.stream()
                .filter(st -> st.check(symbolName) != null)
                .findFirst()
                .map(st -> st.check(symbolName));
    }
}
