package org.isw2.core;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2.changes.controller.*;
import org.isw2.changes.model.Commit;
import org.isw2.changes.model.Version;
import org.isw2.complexity.model.Method;
import org.isw2.core.boundary.GitController;

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
        // Get Commit from GitHub
        GetCommitFromGit getCommitFromGit = new GetCommitFromGit();
        List<Commit> commits = getCommitFromGit.getCommitFromGit(projectName, gitController);
        MergeVersionAndCommit mergeVersionAndCommit = new MergeVersionAndCommit();
        mergeVersionAndCommit.mergeVersionAndCommit(versions, commits);
        MapCommitsAndMethods mapCommitsAndMethods = new MapCommitsAndMethods();
        Map<Version, List<Method>> methodsByVersions = mapCommitsAndMethods.getBasicInfo("/home/emanuele/isw2/temp/BOOKKEEPER", versions, gitController);
        methodsByVersions.forEach((version, methods) -> {
            methods.forEach(method -> {
                System.out.println(version.getName() + " " +  method.getSignature() + method.getChangesMetrics().getMethodHistories());
            });
        });

    }

}