package org.isw2.dataset.factory;

import net.sourceforge.pmd.PmdAnalysis;
import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.metrics.controller.PmdFileAnalyzer;
import org.isw2.dataset.metrics.model.CodeSmell;

import java.util.List;
import java.util.Map;

public class PmdFileAnalyzerFactory extends AbstractControllerFactory<Void, Map<String, List<CodeSmell>>> {
    @Override
    public Controller<Void, Map<String, List<CodeSmell>>> createController() {
        return new PmdFileAnalyzer();
    }
}
