package org.isw2.changes.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.isw2.changes.model.Author;
import org.isw2.changes.model.Commit;
import org.isw2.core.boundary.GitController;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GetCommitFromGit {
    private final List<Commit> cleanedCommits = new ArrayList<>();

    public List<Commit> getCommitFromGit(String projectName, GitController gitController) throws GitAPIException, IOException {
        Git git = gitController.cloneRepository(projectName);
        GetFileDiffBetweenCommit getFileDiffBetweenCommit = new GetFileDiffBetweenCommit();
        Iterable<RevCommit> commits = git.log().call();
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
                if (prevCommit != null) {
                    newCommit.setChanges(getFileDiffBetweenCommit.getFileDiffBetweenCommit(git, prevCommit, commit));
                }
            }
            cleanedCommits.add(newCommit);
        }
        return cleanedCommits;
    }

    private static String formatDate(String dateTime) {
        OffsetDateTime odt = OffsetDateTime.parse(dateTime);
        return odt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
