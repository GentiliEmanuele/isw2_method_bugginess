package org.isw2.dataset.core.controller.context;

import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.jira.model.Version;

import java.util.List;
import java.util.Map;

public record WalkVersionsContext(Map<Commit, Map<MethodKey, Method>> methodsByCommit, List<Version> versions) {
}
