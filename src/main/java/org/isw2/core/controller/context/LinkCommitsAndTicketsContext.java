package org.isw2.core.controller.context;

import org.isw2.git.model.Commit;
import org.isw2.jira.model.Ticket;

import java.util.List;

public record LinkCommitsAndTicketsContext(String projectName, List<Commit> commits, List<Ticket> tickets) {
}
