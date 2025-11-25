package org.isw2.factory;

import org.isw2.core.model.Method;
import org.isw2.metrics.controller.ComputeChangesMetrics;

import java.util.List;
import java.util.Map;

public class ComputeChangesMetricsFactory extends AbstractControllerFactory<Map<String, List<Method>>, Void>{
    @Override
    public Controller<Map<String, List<Method>>, Void> createController() {
        return new ComputeChangesMetrics();
    }
}
