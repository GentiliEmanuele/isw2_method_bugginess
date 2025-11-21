package org.isw2.metrics.complexity.controller;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import org.isw2.metrics.complexity.controller.context.VisitReturn;

public class VisitMethod extends TreeScanner<Void, Void> {

    // Counter for CC
    private int cyclomaticComplexity = 1;

    // Counter for statements count
    private int statementCount = 0;

    // Counters fo Cognitive Complexity
    private int cognitiveComplexity = 0;
    private int nestingLevel = 0;

    public static VisitReturn execute(MethodTree methodTree)  {
        VisitMethod scanner = new VisitMethod();
        if (methodTree.getBody() != null) {
            methodTree.getBody().accept(scanner, null);
        }
        return new VisitReturn(scanner.cyclomaticComplexity, scanner.statementCount, scanner.cognitiveComplexity);
    }

    @Override
    public Void visitIf(IfTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);

        // Visit then branch
        nestingLevel++;
        scan(node.getThenStatement(), aVoid);
        nestingLevel--;

        // Visit else statement
        StatementTree elseStmt = node.getElseStatement();
        if (elseStmt != null) {
            if (elseStmt instanceof IfTree) {
                // If is an else-if statement don't increment the nesting level but visit the next if directly
                scan(elseStmt, aVoid);
            } else {
                // If is an else increment cognitive complexity and visit else statements
                cognitiveComplexity += 1;
                scan(elseStmt, aVoid);
            }
        }
        // Child was manually managed
        return null;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);

        nestingLevel++;
        // Visit body
        super.visitForLoop(node, aVoid);
        nestingLevel--;

        return null;
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);
        nestingLevel++;
        super.visitEnhancedForLoop(node, aVoid);
        nestingLevel--;
        return null;
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);
        nestingLevel++;
        super.visitWhileLoop(node, aVoid);
        nestingLevel--;
        return null;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);
        nestingLevel++;
        super.visitDoWhileLoop(node, aVoid);
        nestingLevel--;
        return null;
    }


    @Override
    public Void visitCase(CaseTree node, Void aVoid) {
        cyclomaticComplexity++;
        return super.visitCase(node, aVoid);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void aVoid) {
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);
        nestingLevel++;
        super.visitSwitch(node, aVoid);
        nestingLevel--;
        return null;
    }

    @Override
    public Void visitBinary(BinaryTree node, Void aVoid) {
        if (node.getKind() == Tree.Kind.CONDITIONAL_AND ||
            node.getKind() == Tree.Kind.CONDITIONAL_OR) {
            cyclomaticComplexity++;
            cognitiveComplexity ++;
        }
        return super.visitBinary(node, aVoid);
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, Void aVoid) {
        cyclomaticComplexity++;
        cognitiveComplexity += (1 + nestingLevel);
        return super.visitConditionalExpression(node, aVoid);
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree node, Void aVoid) {
        statementCount++;
        return super.visitExpressionStatement(node, aVoid);
    }

    @Override
    public Void visitReturn(ReturnTree node, Void p) {
        statementCount++;
        return super.visitReturn(node, p);
    }

    @Override
    public Void visitBreak(BreakTree node, Void p) {
        statementCount++;
        return super.visitBreak(node, p);
    }

    @Override
    public Void visitContinue(ContinueTree node, Void p) {
        statementCount++;
        return super.visitContinue(node, p);
    }

    @Override
    public Void visitThrow(ThrowTree node, Void p) {
        statementCount++;
        return super.visitThrow(node, p);
    }


    @Override
    public Void visitCatch(CatchTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);

        nestingLevel++;
        super.visitCatch(node, aVoid);
        nestingLevel--;
        return null;
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        statementCount++;
        return super.visitVariable(node, p);
    }

    @Override
    public Void visitAssert(AssertTree node, Void p) {
        statementCount++;
        return super.visitAssert(node, p);
    }

}
