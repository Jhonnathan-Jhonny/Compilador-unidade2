package com.uepb;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        CharStream input = CharStreams.fromFileName("teste.txt");
        CompiladoresLexer lexer = new CompiladoresLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CompiladoresParser parser = new CompiladoresParser(tokens);

        ParseTree tree = parser.program(); // regra inicial
        System.out.println(tree.toStringTree(parser));
    }
}
