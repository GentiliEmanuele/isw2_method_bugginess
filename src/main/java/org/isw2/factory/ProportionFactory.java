package org.isw2.factory;

import org.isw2.core.controller.Proportion;
import org.isw2.core.controller.context.ProportionContext;

public class ProportionFactory extends AbstractControllerFactory<ProportionContext, Void> {
    @Override
    public Controller<ProportionContext, Void> createController() {
        return new Proportion();
    }
}
