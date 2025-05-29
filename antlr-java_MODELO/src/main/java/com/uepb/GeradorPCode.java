package com.uepb;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class GeradorPCode extends CompiladoresBaseVisitor<Void> {
    private final List<String> pcode = new ArrayList<>();
    private final Map<String, Integer> variables = new HashMap<>();
    private final Map<String, Integer> labels = new HashMap<>();
    private int nextAddress = 0;
    private int labelCounter = 0;

    public List<String> getPCode() {
        return pcode;
    }

    @Override
    public Void visitProgram(CompiladoresParser.ProgramContext ctx) {
        // Processa todas as instruções apenas uma vez
        for (var stmt : ctx.statement()) {
            visit(stmt);
        }
        pcode.add("stp");
        return null;
    }

    @Override
    public Void visitBlock(CompiladoresParser.BlockContext ctx) {
        for (var stmt : ctx.statement()) {
            visit(stmt);
        }
        return null;
    }

    @Override
    public Void visitVarDeclaration(CompiladoresParser.VarDeclarationContext ctx) {
        String id = ctx.ID().getText();
        if (!variables.containsKey(id)) {
            variables.put(id, nextAddress++);
        }
        if (ctx.expr() != null) {
            visit(ctx.expr());
            pcode.add("lda #" + variables.get(id));
            pcode.add("sto");
        }
        return null;
    }

    @Override
    public Void visitAssignment(CompiladoresParser.AssignmentContext ctx) {
        visit(ctx.expr());
        String id = ctx.ID().getText();
        if (!variables.containsKey(id)) {
            variables.put(id, nextAddress++);
        }
        pcode.add("lda #" + variables.get(id));
        pcode.add("sto");
        return null;
    }

    @Override
    public Void visitPrintStatement(CompiladoresParser.PrintStatementContext ctx) {
        visit(ctx.expr());
        pcode.add("wri");
        return null;
    }

    @Override
    public Void visitInputStatement(CompiladoresParser.InputStatementContext ctx) {
        String id = ctx.ID().getText();
        if (!variables.containsKey(id)) {
            variables.put(id, nextAddress++);
        }
        pcode.add("rd");
        pcode.add("lda #" + variables.get(id));
        pcode.add("sto");
        return null;
    }

    @Override
    public Void visitIfStatement(CompiladoresParser.IfStatementContext ctx) {
        visit(ctx.expr());
        String elseLabel = "L" + labelCounter++;
        String endLabel = "L" + labelCounter++;

        pcode.add("fjp " + elseLabel);
        visit(ctx.statement(0));

        if (ctx.ELSE() != null) {
            pcode.add("pip " + endLabel);
            pcode.add(elseLabel + ":");
            visit(ctx.statement(1));
            pcode.add(endLabel + ":");
        } else {
            pcode.add(elseLabel + ":");
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(CompiladoresParser.WhileStatementContext ctx) {
        String startLabel = "L" + labelCounter++;
        String endLabel = "L" + labelCounter++;

        pcode.add(startLabel + ":");
        visit(ctx.expr());
        pcode.add("fjp " + endLabel);
        visit(ctx.statement());
        pcode.add("pip " + startLabel);
        pcode.add(endLabel + ":");
        return null;
    }

    // Expressões
    @Override
    public Void visitLogicalOrExpr(CompiladoresParser.LogicalOrExprContext ctx) {
        visit(ctx.logicalAndExpr(0));
        for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
            pcode.add("ldc true");
            pcode.add("neq");
            pcode.add("fjp LOR_TRUE_" + labelCounter);
            visit(ctx.logicalAndExpr(i));
            pcode.add("or");
            pcode.add("LOR_TRUE_" + labelCounter++ + ":");
        }
        return null;
    }

    @Override
    public Void visitLogicalAndExpr(CompiladoresParser.LogicalAndExprContext ctx) {
        visit(ctx.equalityExpr(0));
        for (int i = 1; i < ctx.equalityExpr().size(); i++) {
            pcode.add("ldc false");
            pcode.add("neq");
            pcode.add("fjp LAND_FALSE_" + labelCounter);
            visit(ctx.equalityExpr(i));
            pcode.add("and");
            pcode.add("LAND_FALSE_" + labelCounter++ + ":");
        }
        return null;
    }

    @Override
    public Void visitEqualityExpr(CompiladoresParser.EqualityExprContext ctx) {
        visit(ctx.relationalExpr(0));
        if (ctx.relationalExpr().size() > 1) {
            visit(ctx.relationalExpr(1));
            if (ctx.EQ() != null) {
                pcode.add("equ");
            } else {
                pcode.add("neq");
            }
        }
        return null;
    }

    @Override
    public Void visitRelationalExpr(CompiladoresParser.RelationalExprContext ctx) {
        visit(ctx.additiveExpr(0));
        if (ctx.additiveExpr().size() > 1) {
            visit(ctx.additiveExpr(1));
            if (ctx.LT() != null) {
                pcode.add("let");
            } else {
                pcode.add("grt");
            }
        }
        return null;
    }

    @Override
    public Void visitAdditiveExpr(CompiladoresParser.AdditiveExprContext ctx) {
        visit(ctx.multiplicativeExpr(0));
        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            visit(ctx.multiplicativeExpr(i));
            if (ctx.PLUS(i - 1) != null) {
                pcode.add("add");
            } else {
                pcode.add("sub");
            }
        }
        return null;
    }

    @Override
    public Void visitMultiplicativeExpr(CompiladoresParser.MultiplicativeExprContext ctx) {
        visit(ctx.powerExpr(0));
        for (int i = 1; i < ctx.powerExpr().size(); i++) {
            visit(ctx.powerExpr(i));
            if (ctx.MULT(i - 1) != null) {
                pcode.add("mul");
            } else {
                pcode.add("div");
            }
        }
        return null;
    }

    @Override
    public Void visitPowerExpr(CompiladoresParser.PowerExprContext ctx) {
        visit(ctx.unaryExpr());
        if (ctx.powerExpr() != null) {
            // Implementação de potência usando multiplicações (simplificação)
            // Para uma implementação real, seria necessário um loop ou função matemática
            visit(ctx.powerExpr());
            pcode.add("to float"); // Garante que são floats
            pcode.add("to float");
            pcode.add("call POW_FUNCTION"); // Supondo uma função externa para potência
        }
        return null;
    }

    @Override
    public Void visitUnaryExpr(CompiladoresParser.UnaryExprContext ctx) {
        visit(ctx.primaryExpr());
        for (int i = ctx.getChildCount() - 2; i >= 0; i--) {
            String op = ctx.getChild(i).getText();
            if (op.equals("-")) {
                pcode.add("ldc -1");
                pcode.add("mul");
            } else if (op.equals("not")) {
                pcode.add("ldc false");
                pcode.add("equ");
            }
        }
        return null;
    }

    @Override
    public Void visitPrimaryExpr(CompiladoresParser.PrimaryExprContext ctx) {
        if (ctx.NUMBER() != null) {
            pcode.add("ldc " + ctx.NUMBER().getText());
        } else if (ctx.STRING() != null) {
            pcode.add("ldc " + ctx.STRING().getText());
        } else if (ctx.ID() != null) {
            String id = ctx.ID().getText();
            if (!variables.containsKey(id)) {
                variables.put(id, nextAddress++);
            }
            pcode.add("lod #" + variables.get(id));
        } else if (ctx.booleanLiteral() != null) {
            pcode.add("ldc " + ctx.booleanLiteral().getText());
        } else if (ctx.expr() != null) {
            visit(ctx.expr());
        }
        return null;
    }
}