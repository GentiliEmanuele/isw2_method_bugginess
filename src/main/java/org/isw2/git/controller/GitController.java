package org.isw2.git.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2.core.controller.context.EntryPointContext;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ExecutionContext;

import java.io.File;
import java.io.IOException;

public class GitController implements Controller {
    private Git git;

    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof EntryPointContext(String projectName))) {
            throw new IllegalArgumentException("Required params: EntryPointContext. Received: " +
                    (context != null ? context.getClass().getSimpleName() : "null"));
        }
        try {
            cloneRepository(projectName);
        } catch (GitAPIException | IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    public Git getGit() {
        return git;
    }

    private void cloneRepository(String projectName) throws GitAPIException, IOException {
        File dir = new File(formatDirectoryName(projectName));
        if (!dir.exists()) {
            git = Git.cloneRepository().setURI(formatGitHubURL(projectName)).setDirectory(dir).call();
        } else {
            git = Git.open(dir);
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
