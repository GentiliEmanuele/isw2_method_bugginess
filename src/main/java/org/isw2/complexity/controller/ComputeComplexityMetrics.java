package org.isw2.complexity.controller;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import org.isw2.complexity.model.HalsteadComplexity;

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

}
