package org.isw2.whatif.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.jira.model.Version;
import org.isw2.whatif.CoordinatorContext;
import org.isw2.whatif.WhatIfStudyCoordinator;

import java.util.Map;

public class CoordinatorFactory extends AbstractControllerFactory<CoordinatorContext,  Map<Version, Map<MethodKey, Method>>> {
    @Override
    public Controller<CoordinatorContext,  Map<Version, Map<MethodKey, Method>>> createController() {
        return new WhatIfStudyCoordinator();
    }
}
