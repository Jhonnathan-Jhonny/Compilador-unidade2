package com.uepb;

import java.io.*;
import java.util.*;

public class PCodeMachine {
    private final Stack<Object> stack = new Stack<>();
    private final Object[] memory;
    private final Map<String, Integer> labels = new HashMap<>();
    private final List<String> instructions = new ArrayList<>();
    private boolean debugMode;
    private int waitTime;
    private int pc = 0;

    public PCodeMachine(int memSize, boolean debug, int waitTime) {
        this.memory = new Object[memSize];
        this.debugMode = debug;
        this.waitTime = waitTime;
    }

    public static void main(String[] args) throws Exception {
        String inputFile = null;
        boolean debug = true;
        int waitTime = 500;
        int memSize = 250;

        for (String arg : args) {
            if (arg.startsWith("-Input=") || arg.startsWith("-i=")) {
                inputFile = arg.split("=")[1];
            } else if (arg.startsWith("-Debug=") || arg.startsWith("-d=")) {
                debug = Boolean.parseBoolean(arg.split("=")[1]);
            } else if (arg.startsWith("-WaitTime=") || arg.startsWith("-w=")) {
                waitTime = Integer.parseInt(arg.split("=")[1]);
            } else if (arg.startsWith("-MemSize=") || arg.startsWith("-m=")) {
                memSize = Integer.parseInt(arg.split("=")[1]);
            }
        }

        if (inputFile == null) {
            System.err.println("Uso: java -jar pcode.jar -Input=arquivo.pcode [-Debug=true|false] [-WaitTime=ms] [-MemSize=cells]");
            return;
        }

        PCodeMachine machine = new PCodeMachine(memSize, debug, waitTime);
        machine.loadInstructions(inputFile);
        machine.resolveLabels();
        machine.run();
    }

    private void loadInstructions(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                instructions.add(line.trim());
            }
        }
    }

    private void resolveLabels() {
        for (int i = 0; i < instructions.size(); i++) {
            String line = instructions.get(i);
            if (line.endsWith(":")) {
                String label = line.substring(0, line.length() - 1);
                labels.put(label, i);
            }
        }
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);

        while (pc < instructions.size()) {
            String line = instructions.get(pc);

            if (debugMode) {
                System.out.println("PC: " + pc + " | Inst: " + line);
                System.out.println("Stack: " + stack);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (line.endsWith(":")) {
                pc++;
                continue;
            }

            String[] parts = line.split(" ");
            String instr = parts[0];

            try {
                switch (instr) {
                    case "lda":
                        stack.push(Integer.parseInt(parts[1].replace("#", "")));
                        break;

                    case "ldc":
                        String valueStr = line.substring(instr.length()).trim();
                        stack.push(parseValue(valueStr));
                        break;

                    case "lod":
                        int addrLoad = Integer.parseInt(parts[1].replace("#", ""));
                        stack.push(memory[addrLoad]);
                        break;

                    case "sto":
                        checkStackSize(2, instr);
                        int address = (int) stack.pop();
                        Object value = stack.pop();
                        memory[address] = value;
                        break;

                    case "add":
                    case "sub":
                    case "mul":
                    case "div": {
                        checkStackSize(2, instr);
                        double b = toNumber(stack.pop());
                        double a = toNumber(stack.pop());
                        switch (instr) {
                            case "add": stack.push(a + b); break;
                            case "sub": stack.push(a - b); break;
                            case "mul": stack.push(a * b); break;
                            case "div": stack.push(a / b); break;
                        }
                        break;
                    }

                    case "wri":
                        checkStackSize(1, instr);
                        Object val = stack.pop();
                        if (val instanceof Integer && (((Integer) val) == 0 || ((Integer) val) == 1)) {
                            System.out.println(((Integer) val) == 1 ? "true" : "false");
                        } else {
                            System.out.println(val);
                        }
                        break;

                    case "rd":
                        System.out.print("Input: ");
                        String input = scanner.nextLine();
                        stack.push(parseValue(input));
                        break;

                    case "equ":
                    case "neq": {
                        checkStackSize(2, instr);
                        Object right = stack.pop();
                        Object left = stack.pop();
                        stack.push(instr.equals("equ") ? (left.equals(right) ? 1 : 0) : (!left.equals(right) ? 1 : 0));
                        break;
                    }

                    case "and":
                    case "or": {
                        checkStackSize(2, instr);
                        int right = toBoolean(stack.pop());
                        int left = toBoolean(stack.pop());
                        stack.push(instr.equals("and") ? (left != 0 && right != 0 ? 1 : 0) : (left != 0 || right != 0 ? 1 : 0));
                        break;
                    }

                    case "let":
                    case "grt":
                    case "lte":
                    case "gte": {
                        checkStackSize(2, instr);
                        double right = toNumber(stack.pop());
                        double left = toNumber(stack.pop());
                        switch (instr) {
                            case "let": stack.push(left < right ? 1 : 0); break;
                            case "grt": stack.push(left > right ? 1 : 0); break;
                            case "lte": stack.push(left <= right ? 1 : 0); break;
                            case "gte": stack.push(left >= right ? 1 : 0); break;
                        }
                        break;
                    }

                    case "fjp":
                    case "tjp": {
                        checkStackSize(1, instr);
                        int cond = toBoolean(stack.pop());
                        String label = parts[1];
                        if (!labels.containsKey(label)) {
                            throw new RuntimeException("Label não encontrado: " + label);
                        }
                        boolean jump = (instr.equals("fjp") && cond == 0) || (instr.equals("tjp") && cond != 0);
                        if (jump) {
                            pc = labels.get(label);
                            continue;
                        }
                        break;
                    }

                    case "pip":
                        pc = labels.get(parts[1]);
                        continue;

                    case "stp":
                        return;

                    case "to":
                        checkStackSize(1, instr);
                        String targetType = parts[1];
                        Object valToConvert = stack.pop();
                        stack.push(convertType(valToConvert, targetType));
                        break;
                    case "call":
                        if (parts[1].equals("POW_FUNCTION")) {
                            checkStackSize(2, instr);
                            double exponent = toNumber(stack.pop());
                            double base = toNumber(stack.pop());
                            stack.push(Math.pow(base, exponent));
                        } else {
                            throw new RuntimeException("Função desconhecida: " + parts[1]);
                        }
                        break;
                    default:
                        System.err.println("Instrução não reconhecida: " + instr);
                        return;
                }

            } catch (EmptyStackException e) {
                System.err.println("Erro: Pilha vazia ao executar instrução: " + line);
                return;

            } catch (Exception e) {
                System.err.println("Erro ao executar instrução '" + line + "': " + e.getMessage());
                return;
            }

            pc++;
        }
    }

    private double toNumber(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        throw new RuntimeException("Valor não numérico: " + obj);
    }

    private int toBoolean(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue() != 0 ? 1 : 0;
        }
        throw new RuntimeException("Valor não booleano: " + obj);
    }

    private Object parseValue(String val) {
        val = val.trim();

        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("“") && val.endsWith("”"))) {
            // Remove aspas e retorna como string
            return val.substring(1, val.length() - 1);
        }

        if (val.equalsIgnoreCase("true")) return 1;
        if (val.equalsIgnoreCase("false")) return 0;

        try {
            if (val.contains(".")) return Double.parseDouble(val);
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            // Fallback: assume string sem aspas
            return val;
        }
    }


    private Object convertType(Object value, String targetType) {
        switch (targetType) {
            case "int":
                if (value instanceof Double) return ((Double) value).intValue();
                if (value instanceof Integer) return value;
                break;
            case "float":
                if (value instanceof Integer) return ((Integer) value).doubleValue();
                if (value instanceof Double) return value;
                break;
            case "bool":
                if (value instanceof Number) return ((Number) value).intValue() != 0 ? 1 : 0;
                break;
        }
        throw new RuntimeException("Não é possível converter " + value + " para " + targetType);
    }

    private void checkStackSize(int required, String instr) {
        if (stack.size() < required) {
            throw new EmptyStackException();
        }
    }
}
