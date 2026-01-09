package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.metrics.controller.LinkMethodAndSmell;
import org.isw2.dataset.metrics.controller.context.LinkMethodAndSmellContext;

public class LinkMethodAndSmellFactory extends AbstractControllerFactory<LinkMethodAndSmellContext, Void> {
    @Override
    public Controller<LinkMethodAndSmellContext, Void> createController() {
        return new LinkMethodAndSmell();
    }
}
