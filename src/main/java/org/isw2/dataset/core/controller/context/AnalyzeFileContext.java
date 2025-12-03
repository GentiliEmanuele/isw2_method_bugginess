package org.isw2.dataset.core.controller.context;

import org.isw2.dataset.jira.model.Version;

import java.util.List;

public record AnalyzeFileContext(String projectName, List<Version> versions) {
}
