package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.WalkVersions;
import org.isw2.dataset.core.controller.context.WalkVersionsContext;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.jira.model.Version;

import java.util.Map;

public class WalkVersionsFactory extends AbstractControllerFactory<WalkVersionsContext, Map<Version, Map<MethodKey, Method>>> {
    @Override
    public Controller<WalkVersionsContext, Map<Version, Map<MethodKey, Method>>> createController() {
        return new WalkVersions();
    }
}
