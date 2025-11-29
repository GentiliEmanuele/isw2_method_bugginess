package org.isw2.core.controller.context;

import org.isw2.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.jira.model.ReturnTickets;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;

import java.util.List;

public record ProportionContext(List<Version> versions, ReturnTickets returnTickets) {

}
