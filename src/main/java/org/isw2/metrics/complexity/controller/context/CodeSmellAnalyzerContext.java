package org.isw2.metrics.complexity.controller.context;

import org.isw2.core.model.FileClass;
import org.isw2.factory.ExecutionContext;
import org.isw2.jira.model.Version;

import java.util.List;
import java.util.Map;

public record CodeSmellAnalyzerContext(Map<Version, List<FileClass>> fileClassByVersion) implements ExecutionContext {
}
