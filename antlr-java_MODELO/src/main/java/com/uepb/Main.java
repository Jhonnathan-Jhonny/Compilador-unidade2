package com.uepb;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

//public class Main {
//    public static void main(String[] args) throws IOException {
//        // Verifica se o arquivo foi passado como argumento
//        if (args.length == 0) {
//            System.err.println("Uso: java -jar compiler.jar <arquivo.lang>");
//            return;
//        }
//
//        // Lê o conteúdo do arquivo de entrada
//        String inputFile = args[0];
//        String code = Files.readString(Paths.get(inputFile));
//
//        // Cria lexer, parser e analisa o código
//        CharStream input = CharStreams.fromString(code);
//        CompiladoresLexer lexer = new CompiladoresLexer(input);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        CompiladoresParser parser = new CompiladoresParser(tokens);
//
//        // Obtém a árvore de parsing
//        ParseTree tree = parser.program();
//
//        // Executa análise semântica
//        System.out.println("Iniciando análise semântica...");
//        AnalisadorSemantico analisador = new AnalisadorSemantico();
//        analisador.visit(tree);
//        System.out.println("Análise semântica concluída.");
//    }
//}

public class Main {
    public static void main(String[] args) throws IOException {
        // Verifica se o arquivo foi passado como argumento
        if (args.length == 0) {
            System.err.println("Uso: java -jar compiler.jar <arquivo.lang> [-output=<arquivo.pcode>] [-run]");
            return;
        }

        // Lê o conteúdo do arquivo de entrada
        String inputFile = args[0];
        String code = Files.readString(Paths.get(inputFile));

        // Cria lexer, parser e analisa o código
        CharStream input = CharStreams.fromString(code);
        CompiladoresLexer lexer = new CompiladoresLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CompiladoresParser parser = new CompiladoresParser(tokens);

        // Obtém a árvore de parsing
        ParseTree tree = parser.program();

        // Gera o código P-Code
        GeradorPCode gerador = new GeradorPCode();
        gerador.visit(tree);
        List<String> pcode = gerador.getPCode();

        // Define o nome do arquivo de saída
        String outputFile = "output.pcode";
        boolean shouldRun = false;

        for (String arg : args) {
            if (arg.startsWith("-output=")) {
                outputFile = arg.substring("-output=".length());
            } else if (arg.equals("-run")) {
                shouldRun = true;
            }
        }

        // Escreve o código P-Code no arquivo
        Files.write(Paths.get(outputFile), pcode);
        System.out.println("Compilação concluída. Código P-Code gerado em: " + outputFile);

        // Executa o P-Code se a flag -run estiver presente
        if (shouldRun) {
            executePCode(pcode);
        }
    }

    private static void executePCode(List<String> pcode) {
        System.out.println("\nExecutando P-Code:");
        for (String instruction : pcode) {
            System.out.println(">>> " + instruction);
            // Aqui entraria a lógica de execução da instrução
        }
    }
}
