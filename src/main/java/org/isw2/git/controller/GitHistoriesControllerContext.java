package org.isw2.git.controller;

import org.isw2.core.model.Method;
import org.isw2.git.model.Commit;
import org.isw2.jira.model.Version;

import java.util.List;
import java.util.Map;

public record GitHistoriesControllerContext(Map<String, List<Method>> methodsByVersionAndPath, List<Commit> commits, List<Version> versions) {
}
