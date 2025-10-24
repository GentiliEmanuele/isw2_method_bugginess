package org.isw2.complexity;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2.changes.controller.GetCommitFromGit;
import org.isw2.changes.controller.GetVersionsFromJira;
import org.isw2.changes.controller.MergeVersionAndCommit;
import org.isw2.changes.model.Commit;
import org.isw2.changes.model.Version;
import org.isw2.complexity.controller.MethodController;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, GitAPIException {
        String projectName = "BOOKKEEPER";
        // Get version from Jira
        GetVersionsFromJira getVersionsFromJira = new GetVersionsFromJira();
        List<Version> versions = getVersionsFromJira.getVersionsFromJira(projectName);
        // Get Commit from GitHub
        GetCommitFromGit getTagFromGit = new GetCommitFromGit();
        List<Commit> commits = getTagFromGit.getCommitFromGit(projectName);
        MergeVersionAndCommit mergeVersionAndCommit = new MergeVersionAndCommit();
        mergeVersionAndCommit.mergeVersionAndCommit(versions, commits);
        versions.forEach(aux -> System.out.println(aux.toString()));
        // Measure complexity metric for methods
        MethodController methodController = new MethodController();
        // methodController.getAllMethodByProject("/home/emanuele/isw2/temp/BOOKKEEPER");
    }

}