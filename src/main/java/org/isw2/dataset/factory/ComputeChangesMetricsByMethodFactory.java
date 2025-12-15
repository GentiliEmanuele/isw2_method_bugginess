package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.metrics.controller.ComputeChangesMetrics;
import org.isw2.dataset.metrics.controller.context.ComputeChangesContext;

public class ComputeChangesMetricsByMethodFactory extends AbstractControllerFactory<ComputeChangesContext, Void> {
    @Override
    public Controller<ComputeChangesContext, Void> createController() {
        return new ComputeChangesMetrics();
    }
}
