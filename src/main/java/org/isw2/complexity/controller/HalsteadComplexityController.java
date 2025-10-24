package org.isw2.complexity_and_smell_metrics.controller;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import org.isw2.complexity_and_smell_metrics.model.HalsteadComplexity;

import java.util.HashSet;
import java.util.Set;

public class HalsteadComplexityController extends TreeScanner<Void, Void> {
    private static Set<String> uniqueOperators = new HashSet<>();
    private static Set<String> uniqueOperands = new HashSet<>();
    private int totalOperators = 0;
    private int totalOperands = 0;

    @Override
    public Void visitBinary(BinaryTree node, Void aVoid) {
        uniqueOperators.add(node.getKind().toString());
        totalOperators++;
        return super.visitBinary(node, aVoid);
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void aVoid) {
        uniqueOperators.add("ASSIGN");
        totalOperators++;
        return super.visitAssignment(node, aVoid);
    }

    @Override
    public Void visitIdentifier(IdentifierTree node, Void aVoid) {
        uniqueOperands.add(node.getKind().toString());
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

    private HalsteadComplexity getHalstedComplexity() {
        HalsteadComplexity halsteadComplexity = new HalsteadComplexity();
        int n1 = uniqueOperators.size();
        int n2 = uniqueOperands.size();
        int totalOperatorsLocal = totalOperators;
        int totalOperandsLocal = totalOperands;

        int vocabulary = n1 + n2;
        int length = totalOperatorsLocal + totalOperandsLocal;
        double volume = length * (Math.log(vocabulary) / Math.log(2));
        double difficulty = (n1 / 2.0) * (totalOperandsLocal / (double) n2);
        double effort = difficulty * volume;
        double estimatedProgramLength = n1 * Math.log(n1) + n2 * Math.log(n2);
        halsteadComplexity.setVocabulary(vocabulary);
        halsteadComplexity.setProgramLength(length);
        halsteadComplexity.setVolume(volume);
        halsteadComplexity.setDifficulty(difficulty);
        halsteadComplexity.setEffort(effort);
        halsteadComplexity.setEstimatedProgramLength(estimatedProgramLength);
        return halsteadComplexity;
    }

    public static HalsteadComplexity computeHalsteadComplexity(MethodTree methodTree) {
        HalsteadComplexityController scanner = new HalsteadComplexityController();
        if (methodTree.getBody() != null) {
            methodTree.getBody().accept(scanner, null);
        }
        return scanner.getHalstedComplexity();
    }
}
