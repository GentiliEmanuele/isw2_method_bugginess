package org.isw2.core;

import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2.changes.controller.*;
import org.isw2.changes.model.Commit;
import org.isw2.changes.model.Version;
import org.isw2.complexity.model.Method;
import org.isw2.core.boundary.GitController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException, GitAPIException {
        String projectName = "BOOKKEEPER";
        GitController gitController = new GitController();
        // Get version from Jira
        GetVersionsFromJira getVersionsFromJira = new GetVersionsFromJira();
        List<Version> versions = getVersionsFromJira.getVersionsFromJira(projectName);
        // versions.forEach(version -> {System.out.println(version.getName());});

        // Get Commit from GitHub
        GetCommitFromGit getCommitFromGit = new GetCommitFromGit();
        List<Commit> commits = getCommitFromGit.getCommitFromGit(projectName, gitController);
        // commits.forEach(commit -> {System.out.println(commit.getId());});
        MergeVersionAndCommit mergeVersionAndCommit = new MergeVersionAndCommit();
        mergeVersionAndCommit.mergeVersionAndCommit(versions, commits);
        // versions.forEach(version -> {System.out.println(version.toString());});

        MapCommitsAndMethods mapCommitsAndMethods = new MapCommitsAndMethods();
        Map<Version, List<Method>> methodsByVersions = mapCommitsAndMethods.getBasicInfo(versions, gitController);

        methodsByVersions.forEach((version, methods) -> {

            try {
                File folder = new File("output");
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                CSVWriter writer = new CSVWriter(new FileWriter("output/" + version.getName() + ".csv"));

                String[] header = {
                        "ClassName",
                        "Signature",
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
                        "methodHistories",
                        "authors"
                };
                writer.writeNext(header);
                methods.forEach(method -> {
                    String[] row = {
                            method.getClassName(),
                            method.getSignature(),
                            String.valueOf(method.getStartLine()),
                            String.valueOf(method.getEndLine()),
                            String.valueOf(method.getMetrics().getLinesOfCode()),
                            String.valueOf(method.getMetrics().getStatementsCount()),
                            String.valueOf(method.getMetrics().getCyclomaticComplexity()),
                            String.valueOf(method.getMetrics().getCognitiveComplexity()),
                            String.valueOf(method.getMetrics().getHalsteadComplexity().getEffort()),
                            String.valueOf(method.getMetrics().getHalsteadComplexity().getDifficulty()),
                            String.valueOf(method.getMetrics().getHalsteadComplexity().getVolume()),
                            String.valueOf(method.getMetrics().getHalsteadComplexity().getEstimatedProgramLength()),
                            String.valueOf(method.getMetrics().getHalsteadComplexity().getProgramLength()),
                            String.valueOf(method.getMetrics().getHalsteadComplexity().getVocabulary()),
                            String.valueOf(method.getMetrics().getNestingDepth()),
                            String.valueOf(method.getMetrics().getNumberOfBranchesAndDecisionPoint()),
                            String.valueOf(method.getMetrics().getParameterCount()),
                            String.valueOf(method.getChangesMetrics().getMethodHistories()),
                            String.valueOf(method.getChangesMetrics().getAuthors())
                    };
                    writer.writeNext(row);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}