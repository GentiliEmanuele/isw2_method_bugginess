package org.isw2.dataset.metrics.controller.context;

import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodsKey;
import org.isw2.dataset.jira.model.Version;

import java.util.List;
import java.util.Map;

public record ComputeChangesContext(List<Version> versions, Map<MethodsKey, List<Method>> methodByVersionAndPath) {
}
