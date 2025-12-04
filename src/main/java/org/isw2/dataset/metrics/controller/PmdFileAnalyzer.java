package org.isw2.dataset.metrics.controller;

import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.reporting.Report;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.Controller;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.isw2.dataset.metrics.model.CodeSmell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PmdFileAnalyzer implements Controller<PmdAnalysis, Map<String, List<CodeSmell>>> {

    @Override
    public Map<String, List<CodeSmell>> execute(PmdAnalysis pmdAnalysis) throws ProcessingException {
        Map<String, List<CodeSmell>> smellsByPathAndVersion = new HashMap<>();
        Report report = pmdAnalysis.performAnalysisAndCollectReport();
        for (RuleViolation violation : report.getViolations()) {
            String fileId = violation.getFileId().getOriginalPath();
            smellsByPathAndVersion.computeIfAbsent(fileId, k -> new ArrayList<CodeSmell>());
            smellsByPathAndVersion.get(fileId).add(new CodeSmell(violation.getBeginLine(), violation.getEndLine()));
        }
        return smellsByPathAndVersion;
    }
}
