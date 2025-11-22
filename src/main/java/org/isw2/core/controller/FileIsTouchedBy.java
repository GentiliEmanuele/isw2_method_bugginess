package org.isw2.core.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.isw2.core.controller.context.EntryPointContext;
import org.isw2.core.controller.context.MapCommitsAndMethodContext;
import org.isw2.core.model.FileClass;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ExecutionContext;
import org.isw2.git.model.Change;
import org.isw2.git.model.Commit;
import org.isw2.jira.model.Version;
import org.isw2.git.controller.GitController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileIsTouchedBy implements Controller {

    private final Map<Version, List<FileClass>> fileClassByVersion;

    public FileIsTouchedBy() {
        this.fileClassByVersion = new HashMap<>();
    }

    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof MapCommitsAndMethodContext(
                String projectName, List<Version> versions, GitController gitController
        ))) {
            throw new IllegalArgumentException("Required params: MapCommitsContext. Received: " +
                    (context != null ? context.getClass().getSimpleName() : "null"));
        }

        try {
            gitController.execute(new EntryPointContext(projectName));
            walkVersions(
                    getGit(gitController),
                    versions
            );
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }


    private Git getGit(Controller gitController) throws ProcessingException {
        if (gitController instanceof GitController controller) {
            return controller.getGit();
        } else {
            throw new ProcessingException("Processing error was occurred");
        }
    }

    private void walkVersions(Git git, List<Version> versions) throws IOException {
        try (Repository repo = git.getRepository()) {
            for (Version version : versions) {
                List<Commit> commits = version.getCommits();
                List<FileClass> fileClassList = new ArrayList<>();
                for (Commit commit : commits) {
                    analyzeCommit(repo, commit, fileClassList);
                }

                fileClassByVersion.put(version, new ArrayList<>(fileClassList));
            }
        }
    }

    public Map<Version, List<FileClass>> getFileClassByVersion() {
        return fileClassByVersion;
    }

    private void analyzeCommit(Repository repository, Commit commit, List<FileClass> fileClassList) throws IOException {
        ObjectId commitId = repository.resolve(commit.getId()); // Parse commit id into ObjectId
        // Create a walker for iterate the commits
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit revCommit = walk.parseCommit(commitId); // Fetch and decode of the specified commit
            RevTree tree = revCommit.getTree(); // Get the three of the commit
            // Create a walker for walk on the commits' three
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree); // Add the three to be explored
                treeWalk.setRecursive(true); // Set recursive mode for explore also the subdirectories
                while (treeWalk.next()) {
                    String path = treeWalk.getPathString();
                    boolean touched;

                    if (path.endsWith(".java") && !path.endsWith("package-info.java") && commit.getChanges() != null) {
                        touched = fileIsTouchedBy(commit, path);
                        if (!touched) {
                            continue;
                        }

                        String content = getClassContent(repository, treeWalk);
                        FileClass fileClass = new FileClass(path, content);
                        fileClassList.add(fileClass);
                    }
                }
            }
        }
    }

    private String getClassContent(Repository repository, TreeWalk treeWalk) throws IOException {
        ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
        String content;
        try (InputStream is = loader.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            content = contentBuilder.toString();
        }
        return content;
    }

    private boolean fileIsTouchedBy(Commit commit, String classPath) {
        for (Change c : commit.getChanges()) {
            if (fileIsTouchedByAdd(c, classPath) || fileIsTouchedByModify(c, classPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean fileIsTouchedByModify(Change change, String classPath) {
        return change.getType().equals("MODIFY") && change.getOldPath().equals(classPath);
    }

    private boolean fileIsTouchedByAdd(Change change, String classPath) {
        return change.getType().equals("ADD") && change.getNewPath().equals(classPath);
    }

}
