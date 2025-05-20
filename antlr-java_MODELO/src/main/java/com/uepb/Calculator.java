package com.uepb;

import java.util.*;

public class Calculator extends CompiladoresBaseVisitor<Object> {
    private final Map<String, Object> memory = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public Object visitProgram(CompiladoresParser.ProgramContext ctx) {
        for (var stmt : ctx.statement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public Object visitBlock(CompiladoresParser.BlockContext ctx) {
        for (var stmt : ctx.statement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public Object visitVarDeclaration(CompiladoresParser.VarDeclarationContext ctx) {
        String id = ctx.ID().getText();
        Object value = ctx.expr() != null ? visit(ctx.expr()) : null;
        memory.put(id, value);
        return null;
    }

    @Override
    public Object visitAssignment(CompiladoresParser.AssignmentContext ctx) {
        String id = ctx.ID().getText();
        Object value = visit(ctx.expr());
        memory.put(id, value);
        return null;
    }

    @Override
    public Object visitPrintStatement(CompiladoresParser.PrintStatementContext ctx) {
        Object value = visit(ctx.expr());
        System.out.println(value);
        return null;
    }

    @Override
    public Object visitInputStatement(CompiladoresParser.InputStatementContext ctx) {
        String id = ctx.ID().getText();
        System.out.print(id + ": ");
        String input = scanner.nextLine();
        try {
            if (input.contains(".")) {
                memory.put(id, Double.parseDouble(input));
            } else {
                memory.put(id, Integer.parseInt(input));
            }
        } catch (Exception e) {
            memory.put(id, input);
        }
        return null;
    }

    @Override
    public Object visitIfStatement(CompiladoresParser.IfStatementContext ctx) {
        Object cond = visit(ctx.expr());
        if (asBoolean(cond)) {
            visit(ctx.statement(0));
        } else if (ctx.statement().size() > 1) {
            visit(ctx.statement(1));
        }
        return null;
    }

    @Override
    public Object visitWhileStatement(CompiladoresParser.WhileStatementContext ctx) {
        while (asBoolean(visit(ctx.expr()))) {
            visit(ctx.statement());
        }
        return null;
    }

    // Expressões
    @Override
    public Object visitExpr(CompiladoresParser.ExprContext ctx) {
        return visit(ctx.logicalOrExpr());
    }

    @Override
    public Object visitLogicalOrExpr(CompiladoresParser.LogicalOrExprContext ctx) {
        Object result = visit(ctx.logicalAndExpr(0));
        for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
            result = asBoolean(result) || asBoolean(visit(ctx.logicalAndExpr(i)));
        }
        return result;
    }

    @Override
    public Object visitLogicalAndExpr(CompiladoresParser.LogicalAndExprContext ctx) {
        Object result = visit(ctx.equalityExpr(0));
        for (int i = 1; i < ctx.equalityExpr().size(); i++) {
            result = asBoolean(result) && asBoolean(visit(ctx.equalityExpr(i)));
        }
        return result;
    }

    @Override
    public Object visitEqualityExpr(CompiladoresParser.EqualityExprContext ctx) {
        Object left = visit(ctx.relationalExpr(0));
        if (ctx.relationalExpr().size() == 1) return left;
        Object right = visit(ctx.relationalExpr(1));
        return ctx.EQ() != null ? Objects.equals(left, right) : !Objects.equals(left, right);
    }

    @Override
    public Object visitRelationalExpr(CompiladoresParser.RelationalExprContext ctx) {
        Object left = visit(ctx.additiveExpr(0));
        if (ctx.additiveExpr().size() == 1) return left;
        Object right = visit(ctx.additiveExpr(1));
        double l = asDouble(left), r = asDouble(right);
        return ctx.LT() != null ? l < r : l > r;
    }

    @Override
    public Object visitAdditiveExpr(CompiladoresParser.AdditiveExprContext ctx) {
        Object result = visit(ctx.multiplicativeExpr(0));
        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            double left = asDouble(result);
            double right = asDouble(visit(ctx.multiplicativeExpr(i)));
            result = ctx.PLUS(i - 1) != null ? left + right : left - right;
        }
        return result;
    }

    @Override
    public Object visitMultiplicativeExpr(CompiladoresParser.MultiplicativeExprContext ctx) {
        Object result = visit(ctx.powerExpr(0));
        for (int i = 1; i < ctx.powerExpr().size(); i++) {
            double left = asDouble(result);
            double right = asDouble(visit(ctx.powerExpr(i)));
            result = ctx.MULT(i - 1) != null ? left * right : left / right;
        }
        return result;
    }

    @Override
    public Object visitPowerExpr(CompiladoresParser.PowerExprContext ctx) {
        Object base = visit(ctx.unaryExpr());
        if (ctx.powerExpr() != null) {
            return Math.pow(asDouble(base), asDouble(visit(ctx.powerExpr())));
        }
        return base;
    }

    @Override
    public Object visitUnaryExpr(CompiladoresParser.UnaryExprContext ctx) {
        Object value = visit(ctx.primaryExpr());
        for (int i = ctx.getChildCount() - 2; i >= 0; i--) {
            String op = ctx.getChild(i).getText();
            if (op.equals("-")) value = -asDouble(value);
            else if (op.equals("not")) value = !asBoolean(value);
        }
        return value;
    }

    @Override
    public Object visitPrimaryExpr(CompiladoresParser.PrimaryExprContext ctx) {
        if (ctx.NUMBER() != null) {
            return ctx.NUMBER().getText().contains(".") ?
                    Double.parseDouble(ctx.NUMBER().getText()) :
                    Integer.parseInt(ctx.NUMBER().getText());
        } else if (ctx.STRING() != null) {
            return ctx.STRING().getText().replaceAll("^\"|\"$", "");
        } else if (ctx.ID() != null) {
            return memory.getOrDefault(ctx.ID().getText(), 0);
        } else if (ctx.booleanLiteral() != null) {
            return ctx.booleanLiteral().getText().equals("true");
        } else {
            return visit(ctx.expr());
        }
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        return value != null;
    }

    private double asDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        throw new RuntimeException("Esperado número, encontrado: " + value);
    }
}

