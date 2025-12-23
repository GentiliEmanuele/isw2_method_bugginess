package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.FileIsTouchedBy;
import org.isw2.dataset.core.controller.context.TouchedByContext;

public class FileIsTouchedByFactory extends AbstractControllerFactory<TouchedByContext, Boolean> {
    @Override
    public Controller<TouchedByContext, Boolean> createController() {
        return new FileIsTouchedBy();
    }
}
