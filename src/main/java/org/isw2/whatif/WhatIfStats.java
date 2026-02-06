package org.isw2.whatif;

import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.jira.model.Version;

import java.util.Map;

public record WhatIfStats(int expectedBugComplete, int actualBugComplete, int expectedBugWithSmell, int actualBugWithSmell, int expectedBugWithoutSmell, int actualBugWithoutSmell, int actualAfterSmellReset, Map<Version, Map<MethodKey, Method>> refactoredAndNot) {
}
