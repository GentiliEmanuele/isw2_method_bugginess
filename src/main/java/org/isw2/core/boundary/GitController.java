package org.isw2.core.boundary;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

public class GitController {
    private Git git;

    public Git cloneRepository(String projectName) throws GitAPIException, IOException {
        File dir = new File(formatDirectoryName(projectName));
        if (!dir.exists()) {
            git = Git.cloneRepository().setURI(formatGitHubURL(projectName)).setDirectory(dir).call();
        } else {
            git = Git.open(dir);
        }
        return git;
    }

    public Git checkout(String commitId) throws GitAPIException {
        if (git != null) {
            git.checkout().setName(commitId).call();
        }
        return git;
    }

    private static String formatDirectoryName(String projectName) {
        String baseDir = "/home/emanuele/isw2/temp/";
        return baseDir.concat(projectName);
    }

    private static String formatGitHubURL(String projectName) {
        String url = "https://github.com/apache/";
        return url.concat(projectName).concat(".git");
    }
}
