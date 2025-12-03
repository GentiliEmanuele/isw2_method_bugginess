package org.isw2.dataset.core.controller.context;

import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.jira.model.Ticket;

import java.util.List;

public record LinkCommitsAndTicketsContext(String projectName, List<Commit> commits, List<Ticket> tickets) {
}
