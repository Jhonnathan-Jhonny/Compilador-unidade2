package com.uepb;

import org.antlr.v4.runtime.Token;

public class SemanticException extends RuntimeException {
    public SemanticException(String mensage, Token tk) {
        super( String
                 .format( "Erro semântico[%d, %d]: %s",
                 tk.getLine(), tk.getCharPositionInLine(), mensage
             )
        );
    }
}
