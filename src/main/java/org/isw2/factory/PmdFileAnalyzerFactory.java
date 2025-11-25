package org.isw2.factory;

import net.sourceforge.pmd.PmdAnalysis;
import org.isw2.metrics.controller.PmdFileAnalyzer;
import org.isw2.metrics.model.CodeSmell;

import java.util.List;
import java.util.Map;

public class PmdFileAnalyzerFactory extends AbstractControllerFactory<PmdAnalysis, Map<String, List<CodeSmell>>> {
    @Override
    public Controller<PmdAnalysis, Map<String, List<CodeSmell>>> createController() {
        return new PmdFileAnalyzer();
    }
}
