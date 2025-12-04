package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.Proportion;
import org.isw2.dataset.core.controller.context.ProportionContext;

public class ProportionFactory extends AbstractControllerFactory<ProportionContext, Void> {
    @Override
    public Controller<ProportionContext, Void> createController() {
        return new Proportion();
    }
}
