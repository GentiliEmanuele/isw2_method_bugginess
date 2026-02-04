package org.isw2.whatif.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.jira.model.Version;
import org.isw2.whatif.PreliminaryWhatIf;

import java.util.Map;

public class PreliminaryWhatIfFactory extends AbstractControllerFactory<String, Map<Version, Map<MethodKey, Method>>> {
    @Override
    public Controller<String, Map<Version, Map<MethodKey, Method>>> createController() {
        return new PreliminaryWhatIf();
    }
}
