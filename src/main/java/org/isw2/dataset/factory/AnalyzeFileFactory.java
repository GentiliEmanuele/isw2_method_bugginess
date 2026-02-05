package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.AnalyzeFile;
import org.isw2.dataset.core.controller.context.AnalyzeFileContext;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.git.model.Commit;

import java.util.Map;

public class AnalyzeFileFactory extends AbstractControllerFactory<AnalyzeFileContext, Map<Commit, Map<MethodKey, Method>>> {
    @Override
    public Controller<AnalyzeFileContext, Map<Commit, Map<MethodKey, Method>>> createController() {
        return new AnalyzeFile();
    }
}
