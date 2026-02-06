package org.isw2.dataset.factory;

import net.sourceforge.pmd.lang.document.TextFile;
import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.metrics.controller.PmdFileAnalyzer;
import org.isw2.dataset.metrics.model.CodeSmell;

import java.util.List;
import java.util.Map;

public class PmdFileAnalyzerFactory extends AbstractControllerFactory<Map<String, TextFile>, Map<String, List<CodeSmell>>> {
    @Override
    public Controller<Map<String, TextFile>, Map<String, List<CodeSmell>>> createController() {
        return new PmdFileAnalyzer();
    }
}
