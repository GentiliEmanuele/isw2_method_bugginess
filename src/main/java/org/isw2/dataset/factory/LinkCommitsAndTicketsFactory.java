package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.LinkCommitsAndTickets;
import org.isw2.dataset.core.controller.context.LinkCommitsAndTicketsContext;

public class LinkCommitsAndTicketsFactory extends AbstractControllerFactory<LinkCommitsAndTicketsContext, Void> {
    @Override
    public Controller<LinkCommitsAndTicketsContext, Void> createController() {
        return new LinkCommitsAndTickets();
    }
}
