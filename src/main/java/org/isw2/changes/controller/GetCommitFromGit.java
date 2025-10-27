package org.isw2.changes.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.isw2.changes.model.Author;
import org.isw2.changes.model.Change;
import org.isw2.changes.model.Commit;
import org.isw2.core.boundary.GitController;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetCommitFromGit {
    private final List<Commit> cleanedCommits = new ArrayList<>();
    Map<String, List<Change>> diffCache = new HashMap<>();

    public List<Commit> getCommitFromGit(String projectName, GitController gitController) throws GitAPIException, IOException {
        Git git = gitController.cloneRepository(projectName);
        GetFileDiffBetweenCommit getFileDiffBetweenCommit = new GetFileDiffBetweenCommit();
        Iterable<RevCommit> commits = git.log().all().call();
        int length = getCommitsLength(commits);
        commits = git.log().all().call();
        int processed = 0;
        for (RevCommit commit : commits) {
            String commitId = commit.getId().getName();
            String name = commit.getAuthorIdent().getName();
            String authorEmail = commit.getAuthorIdent().getEmailAddress();
            Author author = new Author();
            author.setName(name);
            author.setAuthorEmail(authorEmail);
            Commit newCommit = new Commit();
            newCommit.setId(commitId);
            newCommit.setAuthor(author);
            newCommit.setCommitTime(formatDate(commit.getAuthorIdent().getWhenAsInstant().toString()));

            if (commit.getParents().length != 0) {
                RevCommit prevCommit = commit.getParent(0);
                String key = prevCommit.getName() + "->" + commit.getName();
                // System.out.println("Work with diff between " + prevCommit + " and " + commit.getName());
                if (!diffCache.containsKey(key)) {
                    diffCache.put(key, getFileDiffBetweenCommit.getFileDiffBetweenCommit(git, prevCommit, commit));
                }
                newCommit.setChanges(diffCache.get(key));
            }
            processed++;
            int percent = (processed * 100) / length;
            System.out.print("\rGet commit from git: progress: " + percent + "%");

            cleanedCommits.add(newCommit);
        }
        System.out.println();
        return cleanedCommits;
    }

    private static int getCommitsLength(Iterable<RevCommit> commits) {
        int lenght = 0;
        for (RevCommit commit : commits) {
            lenght++;
        }
        return lenght;
    }

    private static String formatDate(String dateTime) {
        OffsetDateTime odt = OffsetDateTime.parse(dateTime);
        return odt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
