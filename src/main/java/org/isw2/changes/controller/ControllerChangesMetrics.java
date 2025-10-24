package org.isw2.changes.controller;

import org.isw2.changes.model.Version;
import org.isw2.complexity.model.Method;


public class ControllerChangesMetrics {
    private ComputeChangesMetrics computeChangesMetrics = new ComputeChangesMetrics();
    // This method compute changes from the first available version
    public long wrapperComputeMethodHistories(Method method, Version first, Version end) {
        return computeChangesMetrics.computeMethodHistories(method, first, end);
    }

    public int wrapperComputeAuthors(Method method, Version first, Version end) {
        return computeChangesMetrics.computeAuthors(method, first, end);
    }
}
