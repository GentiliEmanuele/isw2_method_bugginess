package org.isw2.dataset.factory;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.AnalyzeFile;
import org.isw2.dataset.core.controller.context.AnalyzeFileContext;
import org.isw2.dataset.core.model.AnalysisResult;

public class AnalyzeFileFactory extends AbstractControllerFactory<AnalyzeFileContext, AnalysisResult> {
    @Override
    public Controller<AnalyzeFileContext, AnalysisResult> createController() {
        return new AnalyzeFile();
    }
}
