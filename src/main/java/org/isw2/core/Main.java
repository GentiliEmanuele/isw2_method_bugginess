package org.isw2.core;

import com.opencsv.CSVWriter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2.changes.controller.*;
import org.isw2.changes.model.Commit;
import org.isw2.jira.controller.GetTicketFromJira;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;
import org.isw2.complexity.model.Method;
import org.isw2.core.boundary.GitController;
import org.isw2.jira.controller.GetVersionsFromJira;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException, GitAPIException {
        String projectName = "BOOKKEEPER";

        // Get version from Jira
        GetVersionsFromJira getVersionsFromJira = new GetVersionsFromJira();
        List<Version> versions = getVersionsFromJira.getVersionsFromJira(projectName);

        // Get ticket from Jira
        GetTicketFromJira getTicketFromJira = new GetTicketFromJira();
        List<Ticket> tickets = getTicketFromJira.getTicketFromJira(projectName, versions);

        // Get Commit from GitHub
        GitController gitController = new GitController();
        GetCommitFromGit getCommitFromGit = new GetCommitFromGit();
        List<Commit> commits = getCommitFromGit.getCommitFromGit(projectName, gitController);

        // Merge version and commit
        MergeVersionAndCommit mergeVersionAndCommit = new MergeVersionAndCommit();
        mergeVersionAndCommit.mergeVersionAndCommit(versions, commits);

        // Map commit, method and tickets
        MapCommitsAndMethods mapCommitsAndMethods = new MapCommitsAndMethods();
        Map<Version, List<Method>> methodsByVersions = mapCommitsAndMethods.getBasicInfo(projectName, versions, gitController, tickets);

        File folder = new File("output");
        if (!folder.exists()) {
            if (!folder.mkdirs()) return;
        }

        CSVWriter writer = new CSVWriter(new FileWriter("output/" + projectName + ".csv"));
        String[] header = {
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
                "maxStmtDeleted",
        };
        writer.writeNext(header);

        methodsByVersions.forEach((version, methods) -> {
            methods.forEach(method -> {
                String[] row = {
                        projectName,
                        method.getClassName(),
                        method.getSignature(),
                        version.getName(),
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
                        String.valueOf(method.getMetrics().getCodeSmellCounter()),
                        String.valueOf(method.getChangesMetrics().getMethodHistories()),
                        String.valueOf(method.getChangesMetrics().getAuthors()),
                        String.valueOf(method.getChangesMetrics().getStmtAdded()),
                        String.valueOf(method.getChangesMetrics().getMaxStmtAdded()),
                        String.valueOf(method.getChangesMetrics().getStmtDeleted()),
                        String.valueOf(method.getChangesMetrics().getMaxStmtDeleted()),
                };
                writer.writeNext(row);
            });
        });
    }
}