package org.isw2.metrics.complexity.controller;


import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.isw2.core.model.FileClass;
import org.isw2.core.model.Method;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ExecutionContext;
import org.isw2.jira.model.Version;
import org.isw2.metrics.complexity.controller.context.CodeSmellAnalyzerContext;
import org.isw2.metrics.complexity.model.CodeSmell;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeSmellAnalyzer implements Controller {
    private final PMDConfiguration config;
    private final LanguageVersion languageVersion;

    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof CodeSmellAnalyzerContext(Map<Version, List<FileClass>> fileClassByVersion))) {
            throw new IllegalArgumentException("Required params: CodeSmellAnalyzerContext. Received: " +
                    (context != null ? context.getClass().getSimpleName() : "null"));
        }
        findSmells(fileClassByVersion);
        mapSmellsAndMethod(fileClassByVersion);
    }

    public CodeSmellAnalyzer() {
        this.config = new PMDConfiguration();
        this.config.addRuleSet("rulesets/java/quickstart.xml");
        this.config.setSourceEncoding(StandardCharsets.UTF_8);
        this.languageVersion = LanguageRegistry.PMD.getLanguageVersionById("java", "1.8");
        this.config.setDefaultLanguageVersion(languageVersion);
        this.config.setFailOnViolation(false);
        this.config.setFailOnError(false);
    }

    private void findSmells(Map<Version, List<FileClass>> fileClassByVersion) {
        Map<String, FileClass> fileRegistry = new HashMap<>();
        try (PmdAnalysis pmdAnalysis= PmdAnalysis.create(this.config)) {
            fileClassByVersion.forEach((version,fileClasses)-> {
                String idVersion = version.getName();
                fileClasses.forEach(fileClass->{
                    String uniqueFileId = idVersion + "_" + fileClass.getPath();
                    TextFile textFile = TextFile.forCharSeq(fileClass.getContent(), FileId.fromPathLikeString(uniqueFileId), this.languageVersion);
                    fileRegistry.put(uniqueFileId, fileClass);
                    fileClass.setSmells(new ArrayList<>());
                    pmdAnalysis.files().addFile(textFile);
                });
            });

            Report report = pmdAnalysis.performAnalysisAndCollectReport();
            for (RuleViolation violation : report.getViolations()) {
                String fileId = violation.getFileId().getOriginalPath();
                FileClass targetFileClass = fileRegistry.get(fileId);
                if (targetFileClass != null) {
                    CodeSmell smell = new CodeSmell(violation.getBeginLine(), violation.getEndLine());
                    targetFileClass.getSmells().add(smell);
                }
            }
        }
    }

    private void mapSmellsAndMethod(Map<Version, List<FileClass>> fileClassByVersion) {
        fileClassByVersion.forEach((_,fileClasses)->
            fileClasses.forEach(fileClass->
                fileClass.getMethods().forEach(method->
                    computeMethodSmells(method, fileClass.getSmells())
                )
            )
        );
    }

    private void computeMethodSmells(Method method, List<CodeSmell> smells) {
        int counter = 0;
        for (CodeSmell smell : smells) {
            if (smell.getStartLine() >= method.getStartLine() && smell.getStartLine() <= method.getEndLine() &&  smell.getEndLine() >= method.getStartLine() && smell.getEndLine() <= method.getEndLine()) {
                counter++;
            }
        }
        method.getMetrics().setCodeSmellCounter(counter);
    }
}
