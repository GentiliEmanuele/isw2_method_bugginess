package org.isw2.dataset.metrics.controller.context;

import org.isw2.dataset.git.model.Commit;

public record ParserContext(String content, String filePath, Commit commit) {
}
