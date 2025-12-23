package org.isw2.dataset.core.controller.context;

import org.isw2.dataset.git.model.Commit;

public record TouchedByContext(Commit commit, String classPath) {
}
