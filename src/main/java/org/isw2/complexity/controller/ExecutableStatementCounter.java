package org.isw2.complexity_and_smell_metrics.controller;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

public class ExecutableStatementCounter extends TreeScanner<Void, Void> {
    private int counterIf = 0;
    private int counterForLoop = 0;
    private int counterEnhancedForLoop = 0;
    private int counterWhileLoop = 0;
    private int counterDoWhileLoop = 0;
    private int counterSwitch = 0;
    private int counterMethodsCall = 0;


    @Override
    public Void visitIf(IfTree node, Void aVoid) {
        counterIf++;
        return super.visitIf(node, aVoid);
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Void aVoid) {
        counterForLoop++;
        return super.visitForLoop(node, aVoid);
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void aVoid) {
        counterEnhancedForLoop++;
        return super.visitEnhancedForLoop(node, aVoid);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void aVoid) {
        counterWhileLoop++;
        return super.visitWhileLoop(node, aVoid);
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void aVoid) {
        counterDoWhileLoop++;
        return super.visitDoWhileLoop(node, aVoid);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void aVoid) {
        counterSwitch++;
        return super.visitSwitch(node, aVoid);
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree node, Void aVoid) {
        // count methods call
        ExpressionTree expr = node.getExpression();
        if (expr instanceof MethodInvocationTree) {
            counterMethodsCall++;
        }
        return super.visitExpressionStatement(node, aVoid);
    }

    private int getCyclomaticComplexity() {
        return counterIf + counterForLoop + counterEnhancedForLoop + counterWhileLoop + counterDoWhileLoop + counterSwitch + 1;
    }

    private int getStatementsCount() {
        return getCyclomaticComplexity() + counterMethodsCall;
    }

    public static int computeCyclomaticComplexity(MethodTree methodTree) {
        ExecutableStatementCounter scanner = new ExecutableStatementCounter();
        if (methodTree.getBody() != null) {
            methodTree.getBody().accept(scanner, null);
        }
        return scanner.getCyclomaticComplexity();
    }

    public static int computeStatementsCount(MethodTree methodTree) {
        ExecutableStatementCounter scanner = new ExecutableStatementCounter();
        if (methodTree.getBody() != null) {
            methodTree.getBody().accept(scanner, null);
        }
        return scanner.getStatementsCount();
    }
}
