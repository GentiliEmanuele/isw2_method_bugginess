package org.isw2.git.controller.context;

import org.isw2.factory.ExecutionContext;
import org.isw2.git.controller.GitController;

public record GetCommitFromGitContext(String projectName, GitController gitController) implements ExecutionContext {
}
