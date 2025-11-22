package org.isw2.metrics.controller.context;

import org.isw2.factory.ExecutionContext;

public record JavaMetricParserContext(String content, String filePath) implements ExecutionContext {
}
