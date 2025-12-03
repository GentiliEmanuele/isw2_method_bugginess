package org.isw2.dataset.factory;

import net.sourceforge.pmd.PmdAnalysis;
import org.isw2.dataset.metrics.controller.PmdFileCollector;
import org.isw2.dataset.metrics.controller.context.PmdFileCollectorContext;

public class PmdFileCollectorFactory extends AbstractControllerFactory<PmdFileCollectorContext, PmdAnalysis> {

    @Override
    public Controller<PmdFileCollectorContext, PmdAnalysis> createController() {
        return PmdFileCollector.getInstance();
    }
}
