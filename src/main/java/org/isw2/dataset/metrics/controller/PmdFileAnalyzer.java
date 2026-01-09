package org.isw2.dataset.metrics.controller;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.reporting.Report;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.Controller;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.isw2.dataset.metrics.model.CodeSmell;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PmdFileAnalyzer implements Controller<Void, Map<String, List<CodeSmell>>> {

    private final PmdAnalysis pmdAnalysis;

    public PmdFileAnalyzer() {
        PMDConfiguration config = new PMDConfiguration();
        config.addRuleSet("rulesets/java/quickstart.xml");
        config.setSourceEncoding(StandardCharsets.UTF_8);
        LanguageVersion languageVersion = LanguageRegistry.PMD.getLanguageVersionById("java", "1.8");
        config.setDefaultLanguageVersion(languageVersion);
        config.setFailOnViolation(false);
        config.setFailOnError(false);
        this.pmdAnalysis = PmdAnalysis.create(config);
    }

    @Override
    public Map<String, List<CodeSmell>> execute(Void context) throws ProcessingException {
        Map<String, List<CodeSmell>> smellsByPathAndVersion = new HashMap<>();
        // Load file for the analysis
        loadFileForPmdAnalysis();
        // Analyze the files
        Report report = pmdAnalysis.performAnalysisAndCollectReport();
        for (RuleViolation violation : report.getViolations()) {
            String fileId = violation.getFileId().getOriginalPath();
            smellsByPathAndVersion.computeIfAbsent(fileId, k -> new ArrayList<>());
            smellsByPathAndVersion.get(fileId).add(new CodeSmell(violation.getBeginLine(), violation.getEndLine()));
        }
        return smellsByPathAndVersion;
    }

    private void loadFileForPmdAnalysis() {
        Map<String, TextFile> contentByVersionsAndPath = PmdFileCollector.getInstance().getContentByVersionAndPath();
        contentByVersionsAndPath.forEach((fileId, textFiles) -> {
            pmdAnalysis.files().addFile(textFiles);
        });
    }
}
