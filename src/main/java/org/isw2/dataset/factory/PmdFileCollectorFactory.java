package org.isw2.dataset.factory;

import net.sourceforge.pmd.PmdAnalysis;
import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.metrics.controller.PmdFileCollector;
import org.isw2.dataset.metrics.controller.context.PmdFileCollectorContext;

public class PmdFileCollectorFactory extends AbstractControllerFactory<PmdFileCollectorContext, Void> {

    @Override
    public Controller<PmdFileCollectorContext, Void> createController() {
        return PmdFileCollector.getInstance();
    }
}
