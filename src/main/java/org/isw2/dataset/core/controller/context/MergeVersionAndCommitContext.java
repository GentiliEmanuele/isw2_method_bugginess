package org.isw2.dataset.core.controller.context;

import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.jira.model.Version;

import java.util.List;

public record MergeVersionAndCommitContext(List<Version> versions, List<Commit> commits) {
}
