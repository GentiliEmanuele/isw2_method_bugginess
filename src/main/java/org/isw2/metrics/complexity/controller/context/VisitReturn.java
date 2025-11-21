package org.isw2.metrics.complexity.controller.context;

public record VisitReturn(int cyclomaticComplexity, int statementCount, int cognitiveComplexity) {
}
