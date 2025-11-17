package org.isw2.metrics.complexity.controller;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import org.isw2.metrics.complexity.model.CodeSmell;
import org.isw2.metrics.complexity.model.HalsteadComplexity;

import java.util.List;

public class ComputeComplexityMetrics {

    public int computeLinesOfCode(MethodTree methodTree) {
        BlockTree blockTree = methodTree.getBody();
        if (blockTree == null) {
            return 0;
        } else {
            return blockTree.getStatements().size();
        }
    }

    public int computeCyclomaticComplexity(MethodTree methodTree) {
        return ExecutableStatementCounter.computeCyclomaticComplexity(methodTree);
    }

    public int computeStatementsCount(MethodTree methodTree) {
        return ExecutableStatementCounter.computeStatementsCount(methodTree);
    }

    public int computeCognitiveComplexity(MethodTree methodTree, CompilationUnitTree compilationUnitTree, JavacTask javacTask) {
        return CognitiveComplexityController.computeCognitiveComplexity(methodTree, compilationUnitTree, javacTask);
    }

    public HalsteadComplexity computeHalstedComplexity(MethodTree methodTree) {
        return HalsteadComplexityController.computeHalsteadComplexity(methodTree);
    }

    public int computeNestingDepth(MethodTree methodTree, CompilationUnitTree compilationUnitTree, JavacTask javacTask) {
        return NestingDepthController.computeNestingDepth(methodTree, compilationUnitTree, javacTask);
    }

    public int computeSmellNumber(List<CodeSmell> smells, int methodStartLine, int methodEndLine) {
        int counter = 0;
        for (CodeSmell smell : smells) {
            if (smell.getStartLine() >= methodStartLine && smell.getStartLine() <= methodEndLine &&  smell.getEndLine() >= methodStartLine && smell.getEndLine() <= methodEndLine) {
                counter++;
            }
        }
        return counter;
    }

}
