package org.isw2.metrics.controller.context;

import org.isw2.git.model.Commit;

public record ParserContext(String content, String filePath, Commit commit) {
}
