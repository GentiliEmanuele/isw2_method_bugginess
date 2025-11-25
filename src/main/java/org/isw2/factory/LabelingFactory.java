package org.isw2.factory;

import org.isw2.core.controller.Labeling;
import org.isw2.core.controller.context.LabelingContext;

public class LabelingFactory extends AbstractControllerFactory<LabelingContext, Void> {
    @Override
    public Controller<LabelingContext, Void> createController() {
        return new Labeling();
    }
}
