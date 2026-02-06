package org.isw2.dataset.factory;

import net.sourceforge.pmd.lang.document.TextFile;
import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.metrics.controller.PmdFilePreparator;
import org.isw2.dataset.metrics.controller.context.PmdFileCollectorContext;

public class PmdFilePreparatorFactory extends AbstractControllerFactory<PmdFileCollectorContext, TextFile> {

    @Override
    public Controller<PmdFileCollectorContext, TextFile> createController() {
        return new PmdFilePreparator();
    }
}
