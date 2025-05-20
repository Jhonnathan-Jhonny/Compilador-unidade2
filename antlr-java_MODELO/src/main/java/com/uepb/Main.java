package com.uepb;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
/*
Fazer o que harlem ensinou em sala: Atribuir nomes as varáveis na gramática
ex:
antes: equalityExpr:  relationalExpr ((EQ | NOTEQ) relationalExpr)?;
depois: equalityExpr: RELATIONAL_EXPR1 = relationalExpr ((EQ | NOTEQ) RELATIONAL_EXPRS = relationalExpr)?;
*/
public class Main {
    public static void main(String[] args) throws IOException {
        CharStream input = CharStreams.fromFileName(args[0]);
        CompiladoresLexer lexer = new CompiladoresLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CompiladoresParser parser = new CompiladoresParser(tokens);
        ParseTree tree = parser.program();

        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Erro de sintaxe. Verifique o código de entrada.");
            return;
        }

        var calculator = new Calculator();
        Double valor = calculator.visit(tree);

        // Só mostra o valor final se não for null
        if (valor != null) {
            System.out.println("Valor final: " + valor);
        }
    }
}