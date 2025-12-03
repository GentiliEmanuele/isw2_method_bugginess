package org.isw2.dataset.git.controller;

import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.jira.model.Version;

import java.util.List;
import java.util.Map;

public record GitHistoriesControllerContext(Map<String, List<Method>> methodsByVersionAndPath, List<Commit> commits, List<Version> versions) {
}
