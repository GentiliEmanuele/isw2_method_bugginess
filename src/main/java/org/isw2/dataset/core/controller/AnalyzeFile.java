package org.isw2.dataset.core.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.context.AnalyzeFileContext;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.factory.*;
import org.isw2.dataset.git.controller.GetCommitFromGit;
import org.isw2.dataset.git.model.Change;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.jira.model.Version;
import org.isw2.dataset.metrics.controller.context.ParserContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AnalyzeFile implements Controller<AnalyzeFileContext, Map<Commit, Map<MethodKey, Method>>> {

    private final Map<Commit, Map<MethodKey, Method>> methodsByCommit;
    private final Map<String, Map<MethodKey, Method>> currentSystemState;

    public AnalyzeFile() {
        methodsByCommit = new HashMap<>();
        currentSystemState = new HashMap<>();
    }

    @Override
    public Map<Commit, Map<MethodKey, Method>> execute(AnalyzeFileContext context) throws ProcessingException {
        try (Git git = GetCommitFromGit.cloneRepository(context.projectName())) {
            processVersions(
                    git.getRepository(),
                    context.versions()
            );
        } catch (IOException | GitAPIException e) {
            throw new ProcessingException(e.getMessage());
        }
        return methodsByCommit;
    }


    private void processVersions(Repository repo, List<Version> versions) throws IOException, ProcessingException {
        for (Version version : versions) {
            for (Commit commit : version.getCommits()) {
                // Apply change to state based on commit changes
                applyChangeToState(repo, commit);
                // Save the current snapshot
                saveSnapshot(commit);
            }
        }
    }

    private void applyChangeToState(Repository repo, Commit commit) throws IOException, ProcessingException {
        ObjectId commitId = repo.resolve(commit.id());
        for (Change change : commit.changes()) {
            String type = change.getType(); // "ADD", "MODIFY", "DELETE", "RENAME"
            String newPath = change.getNewPath();
            String oldPath = change.getOldPath();
            switch (type) {
                case "ADD", "MODIFY", "COPY":
                    // Parse the new content and update the map
                    updateFileMetrics(repo, commitId, newPath);
                    break;

                case "DELETE":
                    // If the file not exist delete it from the map
                    currentSystemState.remove(oldPath);
                    break;

                case "RENAME":
                    // Remove the old entry
                    currentSystemState.remove(oldPath);
                    // Add the new entry
                    updateFileMetrics(repo, commitId, newPath);
                    break;
                default:
                    throw new ProcessingException("Unknown change type: " + type);
            }
        }
    }

    private void saveSnapshot(Commit commit) {
        // Flat the map for the final result
        Map<MethodKey, Method> flatSnapshot = new HashMap<>();

        for (Map<MethodKey, Method> fileMethods : currentSystemState.values()) {
            flatSnapshot.putAll(fileMethods);
        }

        // Save the copy of the current snapshot
        methodsByCommit.put(commit, flatSnapshot);
    }

    private void updateFileMetrics(Repository repo, ObjectId commitId, String filePath) throws IOException, ProcessingException {
        // Get content of the specified file
        String content = getFileContent(repo, commitId, filePath);

        if (content != null && !content.isEmpty()) {
            // Compute the metrics
            AbstractControllerFactory<ParserContext, Map<MethodKey, Method>> parserFactory = new JavaMetricParserFactory();
            Map<MethodKey, Method> methods = parserFactory.process(new ParserContext(content, filePath));

            // Update the global state
            currentSystemState.put(filePath, methods);
        }
    }

    private String getFileContent(Repository repo, ObjectId commitId, String filePath) throws IOException {
        try (TreeWalk treeWalk = TreeWalk.forPath(repo, filePath, repo.parseCommit(commitId).getTree())) {
            if (treeWalk == null) {
                return null;
            }
            ObjectId blobId = treeWalk.getObjectId(0);
            ObjectLoader loader = repo.open(blobId);
            if (loader.isLarge()) return "";
            return new String(loader.getBytes(), StandardCharsets.UTF_8);
        }
    }
}
