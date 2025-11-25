package org.isw2.factory;

import org.isw2.core.controller.AnalyzeFile;
import org.isw2.core.controller.context.AnalyzeFileContext;
import org.isw2.core.model.Method;

import java.util.List;
import java.util.Map;

public class AnalyzeFileFactory extends AbstractControllerFactory<AnalyzeFileContext, Map<String, List<Method>>>{
    @Override
    public Controller<AnalyzeFileContext, Map<String, List<Method>>> createController() {
        return new AnalyzeFile();
    }
}
