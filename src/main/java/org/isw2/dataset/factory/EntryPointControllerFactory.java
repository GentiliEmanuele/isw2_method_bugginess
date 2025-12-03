package org.isw2.dataset.factory;

import org.isw2.dataset.core.controller.EntryPointController;

public class EntryPointControllerFactory extends AbstractControllerFactory<String, Void> {
    @Override
    public Controller<String, Void> createController() {
        return new EntryPointController();
    }
}
