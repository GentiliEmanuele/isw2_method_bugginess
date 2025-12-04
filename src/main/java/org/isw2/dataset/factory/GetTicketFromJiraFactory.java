package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.jira.controller.GetTicketFromJira;
import org.isw2.dataset.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.dataset.jira.model.ReturnTickets;

public class GetTicketFromJiraFactory extends AbstractControllerFactory<GetTicketFromJiraContext, ReturnTickets> {
    @Override
    public Controller<GetTicketFromJiraContext, ReturnTickets> createController() {
        return new GetTicketFromJira();
    }
}
