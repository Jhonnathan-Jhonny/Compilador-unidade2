package com.uepb;

import java.util.*;

public class GeradorPCode extends CompiladoresBaseVisitor<Void> {
    private final List<String> pcode = new ArrayList<>();
    private final Map<String, Integer> variables = new HashMap<>();
    private int nextAddress = 0;
    private int labelCounter = 0;

    public List<String> getPCode() {
        return pcode;
    }

    private void add(String instr) {
        pcode.add(instr);
    }

    @Override
    public Void visitProgram(CompiladoresParser.ProgramContext ctx) {
        for (var stmt : ctx.statement()) {
            visit(stmt);
        }
        add("stp");
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
            add("lda #" + variables.get(id));
            add("sto");
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
        add("lda #" + variables.get(id));
        add("sto");
        return null;
    }

    @Override
    public Void visitPrintStatement(CompiladoresParser.PrintStatementContext ctx) {
        visit(ctx.expr());
        add("wri");
        return null;
    }

    @Override
    public Void visitInputStatement(CompiladoresParser.InputStatementContext ctx) {
        String id = ctx.ID().getText();
        if (!variables.containsKey(id)) {
            variables.put(id, nextAddress++);
        }
        add("rd");
        add("lda #" + variables.get(id));
        add("sto");
        return null;
    }

    @Override
    public Void visitIfStatement(CompiladoresParser.IfStatementContext ctx) {
        String elseLabel = "L" + labelCounter++;
        String endLabel = "L" + labelCounter++;
        visit(ctx.expr());
        add("fjp " + elseLabel);
        visit(ctx.statement(0));
        if (ctx.ELSE() != null) {
            add("pip " + endLabel);
            add(elseLabel + ":");
            visit(ctx.statement(1));
            add(endLabel + ":");
        } else {
            add(elseLabel + ":");
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(CompiladoresParser.WhileStatementContext ctx) {
        String startLabel = "L" + labelCounter++;
        String endLabel = "L" + labelCounter++;
        add(startLabel + ":");
        visit(ctx.expr());
        add("fjp " + endLabel);
        visit(ctx.statement());
        add("pip " + startLabel);
        add(endLabel + ":");
        return null;
    }

    @Override
    public Void visitLogicalOrExpr(CompiladoresParser.LogicalOrExprContext ctx) {
        int count = ctx.logicalAndExpr().size();
        if (count == 1) {
            return visit(ctx.logicalAndExpr(0));
        }

        int label = labelCounter++;
        String trueLabel = "LOR_TRUE_" + label;
        String endLabel = "LOR_END_" + label;

        for (int i = 0; i < count; i++) {
            visit(ctx.logicalAndExpr(i));
            add("tjp " + trueLabel);
        }

        add("ldc false");
        add("pip " + endLabel);

        add(trueLabel + ":");
        add("ldc true");

        add(endLabel + ":");
        return null;
    }

    @Override
    public Void visitLogicalAndExpr(CompiladoresParser.LogicalAndExprContext ctx) {
        int count = ctx.equalityExpr().size();
        if (count == 1) {
            return visit(ctx.equalityExpr(0));
        }

        int label = labelCounter++;
        String falseLabel = "LAND_FALSE_" + label;
        String endLabel = "LAND_END_" + label;

        for (int i = 0; i < count; i++) {
            visit(ctx.equalityExpr(i));
            add("fjp " + falseLabel);
        }

        add("ldc true");
        add("pip " + endLabel);

        add(falseLabel + ":");
        add("ldc false");

        add(endLabel + ":");
        return null;
    }

    @Override
    public Void visitEqualityExpr(CompiladoresParser.EqualityExprContext ctx) {
        visit(ctx.relationalExpr(0));
        if (ctx.relationalExpr().size() > 1) {
            visit(ctx.relationalExpr(1));
            if (ctx.EQ() != null) {
                add("equ");
            } else {
                add("neq");
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
                add("let");
            } else if (ctx.GT() != null) {
                add("grt");
            }
        }
        return null;
    }

    @Override
    public Void visitAdditiveExpr(CompiladoresParser.AdditiveExprContext ctx) {
        visit(ctx.multiplicativeExpr(0));
        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            visit(ctx.multiplicativeExpr(i));
            if (ctx.getChild(2 * i - 1).getText().equals("+")) {
                add("add");
            } else {
                add("sub");
            }
        }
        return null;
    }

    @Override
    public Void visitMultiplicativeExpr(CompiladoresParser.MultiplicativeExprContext ctx) {
        visit(ctx.powerExpr(0));
        for (int i = 1; i < ctx.powerExpr().size(); i++) {
            visit(ctx.powerExpr(i));
            if (ctx.getChild(2 * i - 1).getText().equals("*")) {
                add("mul");
            } else {
                add("div");
            }
        }
        return null;
    }

    @Override
    public Void visitPowerExpr(CompiladoresParser.PowerExprContext ctx) {
        visit(ctx.unaryExpr());
        if (ctx.powerExpr() != null) {
            visit(ctx.powerExpr());
            add("to float");
            add("to float");
            add("call POW_FUNCTION");
        }
        return null;
    }

    @Override
    public Void visitUnaryExpr(CompiladoresParser.UnaryExprContext ctx) {
        visit(ctx.primaryExpr());
        for (int i = ctx.getChildCount() - 2; i >= 0; i--) {
            String op = ctx.getChild(i).getText();
            if (op.equals("-")) {
                add("ldc -1");
                add("mul");
            } else if (op.equals("not")) {
                add("ldc false");
                add("equ");
            }
        }
        return null;
    }

    @Override
    public Void visitPrimaryExpr(CompiladoresParser.PrimaryExprContext ctx) {
        if (ctx.NUMBER() != null) {
            add("ldc " + ctx.NUMBER().getText());
        } else if (ctx.STRING() != null) {
            add("ldc " + ctx.STRING().getText());
        } else if (ctx.ID() != null) {
            String id = ctx.ID().getText();
            if (!variables.containsKey(id)) {
                variables.put(id, nextAddress++);
            }
            add("lod #" + variables.get(id));
        } else if (ctx.booleanLiteral() != null) {
            add("ldc " + ctx.booleanLiteral().getText());
        } else if (ctx.expr() != null) {
            visit(ctx.expr());
        }
        return null;
    }
}
