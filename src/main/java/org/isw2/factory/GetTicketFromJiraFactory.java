package org.isw2.factory;

import org.isw2.jira.controller.GetTicketFromJira;
import org.isw2.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.jira.model.ReturnTickets;
import org.isw2.jira.model.Ticket;

import java.util.List;

public class GetTicketFromJiraFactory extends AbstractControllerFactory<GetTicketFromJiraContext, ReturnTickets> {
    @Override
    public Controller<GetTicketFromJiraContext, ReturnTickets> createController() {
        return new GetTicketFromJira();
    }
}
