package org.isw2.factory;

import org.isw2.jira.controller.GetTicketFromJira;
import org.isw2.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.jira.model.Ticket;

import java.util.List;

public class GetTicketFromJiraFactory extends AbstractControllerFactory<GetTicketFromJiraContext, List<Ticket>> {
    @Override
    public Controller<GetTicketFromJiraContext, List<Ticket>> createController() {
        return new GetTicketFromJira();
    }
}
