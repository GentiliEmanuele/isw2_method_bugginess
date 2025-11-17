package org.isw2.core.controller.context;

import org.isw2.factory.ExecutionContext;
import org.isw2.git.model.Commit;
import org.isw2.jira.model.Version;

import java.util.List;

public record MergeVersionAndCommitContext(List<Version> versions, List<Commit> commits) implements ExecutionContext {
}
