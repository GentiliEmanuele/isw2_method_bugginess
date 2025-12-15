package org.isw2.dataset.core.controller.context;

import org.isw2.dataset.jira.model.Version;

import java.util.List;
import java.util.Map;

public record AnalyzeFileContext(String projectName, List<Version> versions) {
}
