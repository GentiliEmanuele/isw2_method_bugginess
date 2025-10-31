package org.isw2.complexity.controller;


import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.isw2.complexity.model.CodeSmell;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CodeSmellAnalyzer {
    private final PMDConfiguration config;
    private final LanguageVersion  languageVersion;

    public CodeSmellAnalyzer() {
        this.config = new PMDConfiguration();
        // this.config.addRuleSet("category/java/bestpractices.xml");
        this.config.addRuleSet("rulesets/java/quickstart.xml");
        this.config.setSourceEncoding(StandardCharsets.UTF_8);
        this.config.setAnalysisCacheLocation("pmd/pmd.cache");
        this.languageVersion = LanguageRegistry.PMD.getLanguageVersionById("java", "1.8");
        this.config.setDefaultLanguageVersion(languageVersion);
        this.config.setFailOnViolation(false);
        this.config.setFailOnError(false);
    }

    public List<CodeSmell> findSmells(String content) {
        TextFile textFile = TextFile.forCharSeq(content, FileId.fromPathLikeString("temp.java"), this.languageVersion);
        List<CodeSmell> smells = new ArrayList<>();
        try (PmdAnalysis pmdAnalysis= PmdAnalysis.create(this.config)) {
            pmdAnalysis.files().addFile(textFile);
            Report report = pmdAnalysis.performAnalysisAndCollectReport();
            for (RuleViolation violation : report.getViolations()) {
                CodeSmell smell = new CodeSmell(violation.getBeginLine(), violation.getEndLine());
                smells.add(smell);
            }
        }

        return smells;
    }

}
