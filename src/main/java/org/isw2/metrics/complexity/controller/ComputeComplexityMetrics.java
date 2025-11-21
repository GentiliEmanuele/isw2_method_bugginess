package org.isw2.metrics.complexity.controller;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import org.isw2.metrics.complexity.model.CodeSmell;
import org.isw2.metrics.complexity.model.HalsteadComplexity;

import java.util.List;

public class ComputeComplexityMetrics {

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
