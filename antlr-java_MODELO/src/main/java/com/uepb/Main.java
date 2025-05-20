package com.uepb;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        // Verifica se o arquivo foi passado como argumento
        if (args.length == 0) {
            System.err.println("Uso: java -jar compiler.jar <arquivo.lang>");
            return;
        }

        // Lê o conteúdo do arquivo
        String filePath = args[0];
        String code = Files.readString(Paths.get(filePath));

        // Cria lexer, parser e analisa o código
        CharStream input = CharStreams.fromString(code);
        CompiladoresLexer lexer = new CompiladoresLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CompiladoresParser parser = new CompiladoresParser(tokens);

        // Obtém a árvore de parsing
        ParseTree tree = parser.program();

        // Visita com Calculator
        Calculator calculator = new Calculator();
        calculator.visit(tree);
    }
}
