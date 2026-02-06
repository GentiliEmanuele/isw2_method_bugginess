package org.isw2.whatif;

import com.opencsv.CSVWriter;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.jira.model.Version;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class RefactoringStatsToCsv {

    private RefactoringStatsToCsv() {}

    private static final String [] HEADER = {
            "Refactored",
            "ProjectName",
            "ClassName",
            "Signature",
            "LOC",
            "statementsCount",
            "cyclomaticComplexity",
            "cognitiveComplexity",
            "halsteadComplexityEffort",
            "halsteadComplexityDifficulty",
            "halstedComplexityVolume",
            "halsteadEstimatedProgramLength",
            "halsteadProgramLength",
            "halstedVocabulary",
            "nestingDepth",
            "numberOfBranchesAndDecisionPoint",
            "parameterCount",
            "codeSmell",
            "buggy"
    };

    private static final String [] HEADER2 = {
            "A.A",
            "A.E",
            "B+.A",
            "B+.E",
            "B.E",
            "C.A",
            "C.E"
    };

    public static void refactoringStatsToCsv(String projectName, WhatIfStats whatIfStats) throws IOException {
        refactoringMetricStatsToCsv(projectName, whatIfStats.refactoredAndNot());
        refactoringNumericStatsToCsv(projectName, whatIfStats);
    }

    private static void refactoringMetricStatsToCsv(String projectName, Map<Version, Map<MethodKey, Method>> refactoredByVersion) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter("output/" +  projectName + "_refactoring_stats.csv"))) {
            writer.writeNext(HEADER);
            for (Map.Entry<Version, Map<MethodKey, Method>> entry : refactoredByVersion.entrySet()) {
                for (Map.Entry<MethodKey, Method> methodEntry : entry.getValue().entrySet()) {
                    String[] row = {
                            entry.getKey().getName().contains("before") ? "No" : "Yes",
                            projectName,
                            methodEntry.getValue().getMethodKey().className(),
                            methodEntry.getValue().getMethodKey().signature(),
                            String.valueOf(methodEntry.getValue().getMetrics().getLinesOfCode()),
                            String.valueOf(methodEntry.getValue().getMetrics().getStatementsCount()),
                            String.valueOf(methodEntry.getValue().getMetrics().getCyclomaticComplexity()),
                            String.valueOf(methodEntry.getValue().getMetrics().getCognitiveComplexity()),
                            String.valueOf(methodEntry.getValue().getMetrics().getHalsteadComplexity().getEffort()),
                            String.valueOf(methodEntry.getValue().getMetrics().getHalsteadComplexity().getDifficulty()),
                            String.valueOf(methodEntry.getValue().getMetrics().getHalsteadComplexity().getVolume()),
                            String.valueOf(methodEntry.getValue().getMetrics().getHalsteadComplexity().getEstimatedProgramLength()),
                            String.valueOf(methodEntry.getValue().getMetrics().getHalsteadComplexity().getProgramLength()),
                            String.valueOf(methodEntry.getValue().getMetrics().getHalsteadComplexity().getVocabulary()),
                            String.valueOf(methodEntry.getValue().getMetrics().getNestingDepth()),
                            String.valueOf(methodEntry.getValue().getMetrics().getNumberOfBranchesAndDecisionPoint()),
                            String.valueOf(methodEntry.getValue().getMetrics().getParameterCount()),
                            String.valueOf(methodEntry.getValue().getMetrics().getCodeSmellCounter()),
                            String.valueOf(methodEntry.getValue().getBuggy().isBuggy())
                    };
                    writer.writeNext(row);
                }
            }
        }
    }

    private static void refactoringNumericStatsToCsv(String projectName, WhatIfStats whatIfStats) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter("output/" + projectName + "_what-if_stats.csv"))) {
            writer.writeNext(HEADER2);
            String[] row = {
                    String.valueOf(whatIfStats.actualBugComplete()),
                    String.valueOf(whatIfStats.expectedBugComplete()),
                    String.valueOf(whatIfStats.actualBugWithSmell()),
                    String.valueOf(whatIfStats.expectedBugWithSmell()),
                    String.valueOf(whatIfStats.actualAfterSmellReset()),
                    String.valueOf(whatIfStats.actualBugWithoutSmell()),
                    String.valueOf(whatIfStats.expectedBugWithoutSmell())
            };
            writer.writeNext(row);
        }
    }
}
