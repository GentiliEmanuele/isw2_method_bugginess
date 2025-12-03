package org.isw2.dataset.factory;

import org.isw2.dataset.core.controller.AnalyzeFile;
import org.isw2.dataset.core.controller.context.AnalyzeFileContext;
import org.isw2.dataset.core.model.Method;

import java.util.List;
import java.util.Map;

public class AnalyzeFileFactory extends AbstractControllerFactory<AnalyzeFileContext, Map<String, List<Method>>>{
    @Override
    public Controller<AnalyzeFileContext, Map<String, List<Method>>> createController() {
        return new AnalyzeFile();
    }
}
