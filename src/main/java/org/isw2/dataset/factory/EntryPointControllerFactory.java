package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.EntryPointController;
import org.isw2.dataset.core.controller.context.EntryPointContext;

public class EntryPointControllerFactory extends AbstractControllerFactory<EntryPointContext, Void> {
    @Override
    public Controller<EntryPointContext, Void> createController() {
        return new EntryPointController();
    }
}
