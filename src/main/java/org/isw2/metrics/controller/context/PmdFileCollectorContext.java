package org.isw2.metrics.controller.context;

import org.isw2.jira.model.Version;

public record PmdFileCollectorContext(Version version, String path, String content) {
}
