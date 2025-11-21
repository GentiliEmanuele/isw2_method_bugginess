package org.isw2.metrics.complexity.controller.context;

import com.sun.source.tree.MethodTree;
import org.isw2.factory.ExecutionContext;

public record VisitMethodContext(MethodTree methodTree) implements ExecutionContext {
}
