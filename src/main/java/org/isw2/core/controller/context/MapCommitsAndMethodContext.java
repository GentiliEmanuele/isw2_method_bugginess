package org.isw2.core.controller.context;

import org.isw2.factory.ExecutionContext;
import org.isw2.git.controller.GitController;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;

import java.util.List;

public record MapCommitsAndMethodContext(String projectName, List<Version> versions, GitController gitController,
                                         List<Ticket> tickets) implements ExecutionContext {
}
