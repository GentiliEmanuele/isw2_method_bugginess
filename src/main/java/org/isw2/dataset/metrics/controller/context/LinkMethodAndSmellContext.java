package org.isw2.dataset.metrics.controller.context;

import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.jira.model.Version;
import org.isw2.dataset.metrics.model.CodeSmell;

import java.util.List;
import java.util.Map;

public record LinkMethodAndSmellContext(Map<String, List<CodeSmell>> smellsByPathAndVersion, Map<Version, Map<MethodKey, Method>> methodsByVersion) {
}
