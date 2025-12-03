package org.isw2.dataset.metrics.controller.context;

import org.isw2.dataset.jira.model.Version;

public record PmdFileCollectorContext(Version version, String path, String content) {
}
