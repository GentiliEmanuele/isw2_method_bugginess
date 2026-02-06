package org.isw2.whatif.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.whatif.WhatIfStats;
import org.isw2.whatif.context.CoordinatorContext;
import org.isw2.whatif.WhatIfStudyCoordinator;


public class CoordinatorFactory extends AbstractControllerFactory<CoordinatorContext,  WhatIfStats> {
    @Override
    public Controller<CoordinatorContext, WhatIfStats> createController() {
        return new WhatIfStudyCoordinator();
    }
}
