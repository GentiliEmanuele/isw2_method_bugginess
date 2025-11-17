package org.isw2.core.boundary;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class ExitPointBoundary {

    private ExitPointBoundary() {}

    private static final String [] HEADER = {
            "ProjectName",
            "ClassName",
            "Signature",
            "ReleaseID",
            "StartLine",
            "EndLine",
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
            "methodHistories",
            "authors",
            "stmtAdded",
            "maxStmtAdded",
            "stmtDeleted",
            "maxStmtDeleted"
    };

    public static void toCsv(String projectName, List<Outcome> outcomes) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter("output/" + projectName + ".csv"))) {
            writer.writeNext(HEADER);

            outcomes.forEach(outcome -> {
                String[] row = {
                        projectName,
                        outcome.getClassName(),
                        outcome.getSignature(),
                        outcome.getVersion(),
                        String.valueOf(outcome.getStartLine()),
                        String.valueOf(outcome.getEndLine()),
                        String.valueOf(outcome.getLinesOfCode()),
                        String.valueOf(outcome.getStatementsCount()),
                        String.valueOf(outcome.getCyclomaticComplexity()),
                        String.valueOf(outcome.getCognitiveComplexity()),
                        String.valueOf(outcome.getEffort()),
                        String.valueOf(outcome.getDifficulty()),
                        String.valueOf(outcome.getVolume()),
                        String.valueOf(outcome.getEstimatedProgramLength()),
                        String.valueOf(outcome.getProgramLength()),
                        String.valueOf(outcome.getVocabulary()),
                        String.valueOf(outcome.getNestingDepth()),
                        String.valueOf(outcome.getNumberOfBranchesAndDecisionPoint()),
                        String.valueOf(outcome.getParameterCount()),
                        String.valueOf(outcome.getCodeSmellCounter()),
                        String.valueOf(outcome.getMethodHistories()),
                        String.valueOf(outcome.getAuthors()),
                        String.valueOf(outcome.getStmtAdded()),
                        String.valueOf(outcome.getMaxStmtAdded()),
                        String.valueOf(outcome.getStmtDeleted()),
                        String.valueOf(outcome.getMaxStmtDeleted()),
                };
                writer.writeNext(row);
            });
        }
    }
}
