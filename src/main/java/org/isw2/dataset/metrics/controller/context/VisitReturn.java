package org.isw2.dataset.metrics.controller.context;

import org.isw2.dataset.metrics.model.HalsteadComplexity;

public record VisitReturn(int cyclomaticComplexity, int statementCount, int cognitiveComplexity, HalsteadComplexity hc, int nestingDepth) {
}
