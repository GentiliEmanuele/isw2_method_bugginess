package org.isw2.factory;

import org.isw2.core.controller.LinkCommitsAndTickets;
import org.isw2.core.controller.context.LinkCommitsAndTicketsContext;

public class LinkCommitsAndTicketsFactory extends  AbstractControllerFactory<LinkCommitsAndTicketsContext, Void>{
    @Override
    public Controller<LinkCommitsAndTicketsContext, Void> createController() {
        return new LinkCommitsAndTickets();
    }
}
