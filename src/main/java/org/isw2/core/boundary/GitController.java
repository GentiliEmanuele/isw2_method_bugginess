package org.isw2.core.boundary;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2.core.exception.GitDirException;

import java.io.File;
import java.io.IOException;

public class GetterGitInstance {
    private static Git git;

    private GetterGitInstance(String projectName) throws GitDirException, GitAPIException {
        File dir = new File(formatDirectoryName(projectName));
        if (!dir.exists()) {
            GetterGitInstance.git = Git.cloneRepository().setURI(formatGitHubURL(projectName)).setDirectory(dir).call();
        } else {
            try {
                GetterGitInstance.git = Git.open(dir);
            } catch (IOException e) {
                throw new GitDirException(e.getMessage());
            }
        }
    }

    public static Git getterGitInstance(String projectName) {
        if (git == null) {
            return git;
        } else {
            return git;
        }
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
