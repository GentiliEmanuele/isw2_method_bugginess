package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.AnalyzeFile;
import org.isw2.dataset.core.controller.context.AnalyzeFileContext;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodsKey;

import java.util.List;
import java.util.Map;

public class AnalyzeFileFactory extends AbstractControllerFactory<AnalyzeFileContext, Map<MethodsKey, List<Method>>> {
    @Override
    public Controller<AnalyzeFileContext, Map<MethodsKey, List<Method>>> createController() {
        return new AnalyzeFile();
    }
}
