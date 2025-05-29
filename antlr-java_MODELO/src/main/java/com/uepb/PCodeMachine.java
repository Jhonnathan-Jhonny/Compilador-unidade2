package com.uepb;

import java.io.*;
import java.util.*;

public class PCodeMachine {
    private static final Stack<Object> stack = new Stack<>();
    private static final Object[] memory = new Object[300];
    private static final Map<String, Integer> labels = new HashMap<>();
    private static final List<String> instructions = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String inputFile = null;

        for (String arg : args) {
            if (arg.startsWith("-Input=")) {
                inputFile = arg.split("=")[1];
            }
        }

        if (inputFile == null) {
            System.err.println("Uso: java -jar pcode-pcode.jar -Input=arquivo.pcode");
            return;
        }

        loadInstructions(inputFile);
        resolveLabels();
        run();
    }

    private static void loadInstructions(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                instructions.add(line.trim());
            }
        }
    }

    private static void resolveLabels() {
        for (int i = 0; i < instructions.size(); i++) {
            String line = instructions.get(i);
            if (line.endsWith(":")) {
                String label = line.substring(0, line.length() - 1);
                labels.put(label, i);
            }
        }
    }

    private static void run() {
        int pc = 0;
        Scanner scanner = new Scanner(System.in);

        while (pc < instructions.size()) {
            String line = instructions.get(pc);

            if (line.endsWith(":")) {
                pc++;
                continue;
            }

            String[] parts = line.split(" ");
            String instr = parts[0];

            switch (instr) {
                case "to":
                    String targetType = parts[1];
                    Object top = stack.pop();

                    switch (targetType) {
                        case "float":
                            if (top instanceof Integer) {
                                stack.push(((Integer) top).doubleValue());
                            } else if (top instanceof Double) {
                                stack.push(top); // já é float
                            } else {
                                System.err.println("Erro: não é possível converter para float: " + top);
                                return;
                            }
                            break;
                        case "int":
                            if (top instanceof Double) {
                                stack.push(((Double) top).intValue());
                            } else if (top instanceof Integer) {
                                stack.push(top); // já é inteiro
                            } else {
                                System.err.println("Erro: não é possível converter para int: " + top);
                                return;
                            }
                            break;
                        default:
                            System.err.println("Conversão de tipo não suportada: " + targetType);
                            return;
                    }
                    break;
                case "call":
                    if ("POW_FUNCTION".equals(parts[1])) {
                        double exponent = ((Number) stack.pop()).doubleValue();
                        double base = ((Number) stack.pop()).doubleValue();
                        stack.push(Math.pow(base, exponent));
                    } else {
                        System.err.println("Função desconhecida: " + parts[1]);
                        return;
                    }
                    break;
                case "lda":
                    int addr = Integer.parseInt(parts[1].replace("#", ""));
                    stack.push(addr);
                    break;
                case "ldc":
                    stack.push(parseValue(parts[1]));
                    break;
                case "lod":
                    int addrLoad = Integer.parseInt(parts[1].replace("#", ""));
                    stack.push(memory[addrLoad]);
                    break;
                case "sto":
                    int address = (int) stack.pop();
                    Object value = stack.pop();
                    memory[address] = value;
                    break;
                case "add":
                    stack.push(((Number) stack.pop()).doubleValue() + ((Number) stack.pop()).doubleValue());
                    break;
                case "sub":
                    double b = ((Number) stack.pop()).doubleValue();
                    double a = ((Number) stack.pop()).doubleValue();
                    stack.push(a - b);
                    break;
                case "mul":
                    stack.push(((Number) stack.pop()).doubleValue() * ((Number) stack.pop()).doubleValue());
                    break;
                case "div":
                    double divisor = ((Number) stack.pop()).doubleValue();
                    double dividend = ((Number) stack.pop()).doubleValue();
                    stack.push(dividend / divisor);
                    break;
                case "wri":
                    System.out.println(stack.pop());
                    break;
                case "rd":
                    System.out.print("Input: ");
                    String input = scanner.nextLine();
                    if (input.contains(".")) {
                        stack.push(Double.parseDouble(input));
                    } else {
                        stack.push(Integer.parseInt(input));
                    }
                    break;
                case "equ":
                    stack.push(stack.pop().equals(stack.pop()) ? 1 : 0);
                    break;
                case "neq":
                    stack.push(!stack.pop().equals(stack.pop()) ? 1 : 0);
                    break;
                case "and":
                    stack.push(((int) stack.pop() != 0 && (int) stack.pop() != 0) ? 1 : 0);
                    break;
                case "or":
                    stack.push(((int) stack.pop() != 0 || (int) stack.pop() != 0) ? 1 : 0);
                    break;
                case "let":
                    double r = ((Number) stack.pop()).doubleValue();
                    double l = ((Number) stack.pop()).doubleValue();
                    stack.push(l < r ? 1 : 0);
                    break;
                case "grt":
                    r = ((Number) stack.pop()).doubleValue();
                    l = ((Number) stack.pop()).doubleValue();
                    stack.push(l > r ? 1 : 0);
                    break;
                case "fjp":
                    Object cond = stack.pop();
                    if (cond instanceof Number && ((Number) cond).doubleValue() == 0) {
                        pc = labels.get(parts[1]);
                        continue;
                    }
                    break;
                case "pip":
                    pc = labels.get(parts[1]);
                    continue;
                case "stp":
                    return;
                default:
                    System.err.println("Instrução não reconhecida: " + instr);
                    return;
            }

            pc++;
        }
    }

    private static Object parseValue(String val) {
        if (val.equals("true")) return 1;
        if (val.equals("false")) return 0;
        if (val.startsWith("\"") && val.endsWith("\"")) return val.substring(1, val.length() - 1);
        if (val.contains(".")) return Double.parseDouble(val);
        return Integer.parseInt(val);
    }
}
