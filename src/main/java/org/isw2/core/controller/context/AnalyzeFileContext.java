package org.isw2.core.controller.context;

import org.isw2.jira.model.Version;

import java.util.List;

public record AnalyzeFileContext(String projectName, List<Version> versions) {
}
