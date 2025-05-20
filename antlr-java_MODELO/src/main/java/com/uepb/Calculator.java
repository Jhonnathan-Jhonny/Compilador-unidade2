package com.uepb;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Calculator extends CompiladoresBaseVisitor<Double> {

    private final Map<String, Double> variables = new HashMap<>();
    private final Scope scope = new Scope(new LinkedList<>());

    @Override
    public Double visitProgram(CompiladoresParser.ProgramContext ctx) {
        scope.startScope();
        Double lastValue = null;
        for (var stmt : ctx.statement()) {
            Double result = visit(stmt);
            if (result != null) {
                lastValue = result;
            }
        }
        return lastValue;
    }

    @Override
    public Double visitVarDeclaration(CompiladoresParser.VarDeclarationContext ctx) {
        String varName = ctx.ID().getText();
        Double value = ctx.expr() != null ? visit(ctx.expr()) : 0.0;
        variables.put(varName, value);
        return value;
    }

    @Override
    public Double visitAssignment(CompiladoresParser.AssignmentContext ctx) {
        String varName = ctx.ID().getText();
        Double value = visit(ctx.expr());
        if (!variables.containsKey(varName)) {
            throw new SemanticException("Variável não declarada: " + varName, ctx.ID().getSymbol());
        }
        variables.put(varName, value);
        return value;
    }

    @Override
    public Double visitPrintStatement(CompiladoresParser.PrintStatementContext ctx) {
        Double value = visit(ctx.expr());
        System.out.println(value);
        return null;
    }

    @Override
    public Double visitInputStatement(CompiladoresParser.InputStatementContext ctx) {
        String varName = ctx.ID().getText();
        Double value = 0.0; // Entrada simulada
        variables.put(varName, value);
        return value;
    }

    @Override
    public Double visitIfStatement(CompiladoresParser.IfStatementContext ctx) {
        boolean condition = visit(ctx.expr()) != 0.0;
        if (condition) {
            return visit(ctx.statement(0));
        } else if (ctx.ELSE() != null) {
            return visit(ctx.statement(1));
        }
        return 0.0;
    }

    @Override
    public Double visitWhileStatement(CompiladoresParser.WhileStatementContext ctx) {
        Double result = 0.0;
        while (visit(ctx.expr()) != 0.0) {
            result = visit(ctx.statement());
        }
        return result;
    }

    @Override
    public Double visitLogicalOrExpr(CompiladoresParser.LogicalOrExprContext ctx) {
        Double result = visit(ctx.logicalAndExpr(0));
        for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
            if (result != 0.0) return 1.0;
            result = visit(ctx.logicalAndExpr(i));
        }
        return result != 0.0 ? 1.0 : 0.0;
    }

    @Override
    public Double visitLogicalAndExpr(CompiladoresParser.LogicalAndExprContext ctx) {
        Double result = visit(ctx.equalityExpr(0));
        for (int i = 1; i < ctx.equalityExpr().size(); i++) {
            if (result == 0.0) return 0.0;
            result = visit(ctx.equalityExpr(i));
        }
        return result != 0.0 ? 1.0 : 0.0;
    }

    @Override
    public Double visitEqualityExpr(CompiladoresParser.EqualityExprContext ctx) {
        Double left = visit(ctx.relationalExpr(0));
        if (ctx.EQ() != null) {
            Double right = visit(ctx.relationalExpr(1));
            return left.equals(right) ? 1.0 : 0.0;
        } else if (ctx.NOTEQ() != null) {
            Double right = visit(ctx.relationalExpr(1));
            return !left.equals(right) ? 1.0 : 0.0;
        }
        return left;
    }

    @Override
    public Double visitRelationalExpr(CompiladoresParser.RelationalExprContext ctx) {
        Double left = visit(ctx.additiveExpr(0));
        if (ctx.LT() != null) {
            return left < visit(ctx.additiveExpr(1)) ? 1.0 : 0.0;
        } else if (ctx.GT() != null) {
            return left > visit(ctx.additiveExpr(1)) ? 1.0 : 0.0;
        }
        return left;
    }

    @Override
    public Double visitAdditiveExpr(CompiladoresParser.AdditiveExprContext ctx) {
        Double result = visit(ctx.multiplicativeExpr(0));
        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            if (ctx.getChild(2 * i - 1).getText().equals("+")) {
                result += visit(ctx.multiplicativeExpr(i));
            } else {
                result -= visit(ctx.multiplicativeExpr(i));
            }
        }
        return result;
    }

    @Override
    public Double visitMultiplicativeExpr(CompiladoresParser.MultiplicativeExprContext ctx) {
        Double result = visit(ctx.powerExpr(0));
        for (int i = 1; i < ctx.powerExpr().size(); i++) {
            if (ctx.getChild(2 * i - 1).getText().equals("*")) {
                result *= visit(ctx.powerExpr(i));
            } else {
                result /= visit(ctx.powerExpr(i));
            }
        }
        return result;
    }

    @Override
    public Double visitPowerExpr(CompiladoresParser.PowerExprContext ctx) {
        if (ctx.POW() != null) {
            return Math.pow(visit(ctx.unaryExpr()), visit(ctx.powerExpr()));
        }
        return visit(ctx.unaryExpr());
    }

    @Override
    public Double visitUnaryExpr(CompiladoresParser.UnaryExprContext ctx) {
        // Último filho sempre será o primaryExpr
        Double value = visit(ctx.getChild(ctx.getChildCount() - 1));
        for (int i = ctx.getChildCount() - 2; i >= 0; i--) {
            String op = ctx.getChild(i).getText();
            switch (op) {
                case "-": value = -value; break;
                case "+": break; // nada
                case "not": value = value == 0.0 ? 1.0 : 0.0; break;
            }
        }
        return value;
    }

    @Override
    public Double visitPrimaryExpr(CompiladoresParser.PrimaryExprContext ctx) {
        if (ctx.NUMBER() != null) {
            return Double.parseDouble(ctx.NUMBER().getText());
        } else if (ctx.ID() != null) {
            String varName = ctx.ID().getText();
            if (!variables.containsKey(varName)) {
                throw new SemanticException("Variável não declarada: " + varName, ctx.ID().getSymbol());
            }
            return variables.get(varName);
        } else if (ctx.expr() != null) {
            return visit(ctx.expr());
        } else if (ctx.booleanLiteral() != null) {
            return visitBooleanLiteral(ctx.booleanLiteral());
        }
        return 0.0;
    }

    @Override
    public Double visitBooleanLiteral(CompiladoresParser.BooleanLiteralContext ctx) {
        return ctx.TRUE() != null ? 1.0 : 0.0;
    }
}
