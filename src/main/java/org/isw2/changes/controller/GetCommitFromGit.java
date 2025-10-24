package org.isw2.changes.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.isw2.changes.model.Author;
import org.isw2.changes.model.Commit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetTagFromGit {
    private List<Commit> cleanedCommits = new ArrayList<>();

    public List<Commit> getCommitFromGit(String projectName) throws GitAPIException, IOException {
        File dir = new File(formatDirectoryName(projectName));
        Git git;
        if (!dir.exists()) {
            git = Git.cloneRepository().setURI(formatGitHubURL(projectName)).setDirectory(dir).call();
        } else {
            git = Git.open(dir);
        }
        List<Ref> tagList = git.tagList().call();
        for (int i = 0; i < tagList.size() - 1; i++) {
            ObjectId start = tagList.get(i).getPeeledObjectId();
            ObjectId end = tagList.get(i + 1).getPeeledObjectId();
            Iterable<RevCommit> commits;
            if (start != null && end != null) {
                commits = git.log().addRange(start, end).call();
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
                    newCommit.setCommitTime(commit.getAuthorIdent().getWhenAsInstant().toString());
                    cleanedCommits.add(newCommit);
                }
            }
        }
        return cleanedCommits;
    }

    private String formatGitHubURL(String projectName) {
        String url = "https://github.com/apache/";
        return url.concat(projectName).concat(".git");
    }

    private String formatDirectoryName(String projectName) {
        String baseDir = "/home/emanuele/isw2/temp/";
        return baseDir.concat(projectName);
    }
}
