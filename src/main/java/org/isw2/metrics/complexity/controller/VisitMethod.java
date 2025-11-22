package org.isw2.metrics.complexity.controller;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import org.isw2.metrics.complexity.controller.context.VisitReturn;
import org.isw2.metrics.complexity.model.HalsteadComplexity;

import java.util.HashSet;
import java.util.Set;

public class VisitMethod extends TreeScanner<Void, Void> {

    // Counter for CC
    private int cyclomaticComplexity = 1;

    // Counter for statements count
    private int statementCount = 0;

    // Counters fo Cognitive Complexity
    private int cognitiveComplexity = 0;
    private int nestingLevel = 0;

    // Counter for nesting depth metric
    private int maxNestingLevel = 0;

    // Halstead state
    private final Set<String> uniqueOperators = new HashSet<>();
    private final Set<String> uniqueOperands = new HashSet<>();
    private int totalOperators = 0;
    private int totalOperands = 0;

    public static VisitReturn execute(MethodTree methodTree)  {
        VisitMethod scanner = new VisitMethod();
        if (methodTree.getBody() != null) {
            scanner.scan(methodTree, null);
        }
        HalsteadComplexity hc = scanner.computeHalsteadComplexity();
        return new VisitReturn(scanner.cyclomaticComplexity, scanner.statementCount, scanner.cognitiveComplexity, hc, scanner.maxNestingLevel);
    }

    private HalsteadComplexity computeHalsteadComplexity() {
        HalsteadComplexity hc = new HalsteadComplexity();
        int n1 = uniqueOperators.size();
        int n2 = uniqueOperands.size();
        int localTotalOperators = totalOperators;
        int localTotalOperands = totalOperands;

        if (n1 == 0 || n2 == 0) return hc;

        int vocabulary = n1 + n2;
        int length = localTotalOperators + localTotalOperands;
        double volume = length * (Math.log(vocabulary) / Math.log(2));
        double difficulty = (n1 / 2.0) * (localTotalOperands / (double) n2);
        double effort = difficulty * volume;
        double estimatedLength = n1 * (Math.log(n1)/Math.log(2)) + n2 * (Math.log(n2)/Math.log(2));

        hc.setVocabulary(vocabulary);
        hc.setProgramLength(length);
        hc.setVolume(volume);
        hc.setDifficulty(difficulty);
        hc.setEffort(effort);
        hc.setEstimatedProgramLength(estimatedLength);
        return hc;
    }

    private void enterScope() {
        nestingLevel++;
        if (nestingLevel > maxNestingLevel) {
            maxNestingLevel = nestingLevel;
        }
    }

    private void exitScope() {
        nestingLevel--;
    }

    @Override
    public Void visitIf(IfTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);

        // Count if as Halstead operator
        uniqueOperators.add("if");
        totalOperators++;
        // Visit condition for Halstead complexity
        scan(node.getCondition(), aVoid);

        // Visit then branch
        enterScope();
        scan(node.getThenStatement(), aVoid);
        exitScope();

        // Visit else statement
        StatementTree elseStmt = node.getElseStatement();
        if (elseStmt != null) {
            if (elseStmt instanceof IfTree) {
                // If is an else-if statement don't increment the nesting level but visit the next if directly
                scan(elseStmt, aVoid);
            } else {
                // If is an else increment cognitive complexity and visit else statements
                cognitiveComplexity += 1;
                enterScope();
                scan(elseStmt, aVoid);
                exitScope();
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

        uniqueOperators.add("for");
        totalOperators++;

        // Visit initialization, condition and update for Halstead complexity
        scan(node.getInitializer(), aVoid);
        scan(node.getCondition(), aVoid);
        scan(node.getUpdate(), aVoid);

        enterScope();
        // Visit body
        scan(node.getStatement(), aVoid);
        exitScope();

        return null;
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);

        uniqueOperators.add("for");
        totalOperators++;

        // Visit variable and expression for Halstead complexity
        scan(node.getVariable(), aVoid);
        scan(node.getExpression(), aVoid);

        enterScope();
        scan(node.getStatement(), aVoid);
        exitScope();
        return null;
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);

        uniqueOperators.add("while");
        totalOperators++;

        // Visit condition for Halstead complexity
        scan(node.getCondition(), aVoid);

        enterScope();
        scan(node.getStatement(), aVoid);
        exitScope();
        return null;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);

        uniqueOperators.add("do");
        totalOperators++;

        // Visit condition for Halstead complexity
        scan(node.getCondition(), aVoid);

        enterScope();
        scan(node.getStatement(), aVoid);
        exitScope();
        return null;
    }


    @Override
    public Void visitCase(CaseTree node, Void aVoid) {
        cyclomaticComplexity++;
        uniqueOperators.add("case");
        totalOperators++;
        return super.visitCase(node, aVoid);
    }

    @Override
    public Void visitSwitch(SwitchTree node, Void aVoid) {
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);

        uniqueOperators.add("switch");
        totalOperators++;

        scan(node.getExpression(), aVoid);

        enterScope();
        scan(node.getCases(), aVoid);
        exitScope();
        return null;
    }

    @Override
    public Void visitBinary(BinaryTree node, Void aVoid) {
        if (node.getKind() == Tree.Kind.CONDITIONAL_AND ||
            node.getKind() == Tree.Kind.CONDITIONAL_OR) {
            cyclomaticComplexity++;
            cognitiveComplexity ++;
        }
        uniqueOperators.add(node.getKind().toString());
        totalOperators++;
        return super.visitBinary(node, aVoid);
    }

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, Void aVoid) {
        cyclomaticComplexity++;
        cognitiveComplexity += (1 + nestingLevel);

        // Consider ternary operator for Halstead complexity
        uniqueOperators.add("?:");
        totalOperators++;

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
        uniqueOperators.add("return");
        totalOperators++;
        return super.visitReturn(node, p);
    }

    @Override
    public Void visitBreak(BreakTree node, Void p) {
        statementCount++;
        uniqueOperators.add("break");
        totalOperators++;
        return super.visitBreak(node, p);
    }

    @Override
    public Void visitContinue(ContinueTree node, Void p) {
        statementCount++;
        uniqueOperators.add("continue");
        totalOperators++;
        return super.visitContinue(node, p);
    }

    @Override
    public Void visitThrow(ThrowTree node, Void p) {
        statementCount++;
        uniqueOperators.add("throw");
        totalOperators++;
        return super.visitThrow(node, p);
    }


    @Override
    public Void visitCatch(CatchTree node, Void aVoid) {
        cyclomaticComplexity++;
        statementCount++;
        cognitiveComplexity += (1 + nestingLevel);

        uniqueOperators.add("catch");
        totalOperators++;

        enterScope();
        super.visitCatch(node, aVoid);
        exitScope();
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

    @Override
    public Void visitIdentifier(IdentifierTree node, Void aVoid) {
        uniqueOperands.add(node.getName().toString());
        totalOperands++;
        return super.visitIdentifier(node, aVoid);
    }

    @Override
    public Void visitLiteral(LiteralTree node, Void p) {
        if (node.getValue() != null) {
            uniqueOperands.add(node.getValue().toString());
            totalOperands++;
        }
        return super.visitLiteral(node, p);
    }

    @Override
    public Void visitUnary(UnaryTree node, Void aVoid) {
        uniqueOperators.add(node.getKind().toString());
        totalOperators++;
        return super.visitUnary(node, aVoid);
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void aVoid) {
        uniqueOperators.add("ASSIGN");
        totalOperators++;
        return super.visitAssignment(node, aVoid);
    }

    @Override
    public Void visitCompoundAssignment(CompoundAssignmentTree node, Void aVoid) {
        uniqueOperators.add(node.getKind().toString());
        totalOperators++;
        return super.visitCompoundAssignment(node, aVoid);
    }

    @Override
    public Void visitMemberSelect(MemberSelectTree node, Void aVoid) {
        uniqueOperators.add(".");
        totalOperators++;
        return super.visitMemberSelect(node, aVoid);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void aVoid) {
        uniqueOperators.add("METHOD_CALL");
        totalOperators++;
        return super.visitMethodInvocation(node, aVoid);
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void aVoid) {
        uniqueOperators.add("new");
        totalOperators++;
        return super.visitNewClass(node, aVoid);
    }

    @Override
    public Void visitTry(TryTree node, Void aVoid) {
        uniqueOperators.add("try");
        totalOperators++;
        return super.visitTry(node, aVoid);
    }

}
