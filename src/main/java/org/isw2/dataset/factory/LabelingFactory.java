package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.Labeling;
import org.isw2.dataset.core.controller.context.LabelingContext;

public class LabelingFactory extends AbstractControllerFactory<LabelingContext, Void> {
    @Override
    public Controller<LabelingContext, Void> createController() {
        return new Labeling();
    }
}
