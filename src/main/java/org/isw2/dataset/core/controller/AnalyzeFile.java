package org.isw2.dataset.core.controller;

import net.sourceforge.pmd.PmdAnalysis;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.context.AnalyzeFileContext;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodsKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.factory.*;
import org.isw2.dataset.git.controller.GetCommitFromGit;
import org.isw2.dataset.git.model.Change;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.jira.model.Version;
import org.isw2.dataset.metrics.controller.context.PmdFileCollectorContext;
import org.isw2.dataset.metrics.controller.context.ParserContext;
import org.isw2.dataset.metrics.model.CodeSmell;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AnalyzeFile implements Controller<AnalyzeFileContext, Map<MethodsKey, List<Method>>> {

    private final Map<MethodsKey, List<Method>> methodsByFileAndVersion;

    public AnalyzeFile() {
        methodsByFileAndVersion = new HashMap<>();
    }

    @Override
    public Map<MethodsKey, List<Method>> execute(AnalyzeFileContext context) throws ProcessingException {
        try {
            walkVersions(
                    GetCommitFromGit.cloneRepository(context.projectName()),
                    context.versions()
            );
        } catch (IOException | GitAPIException e) {
            throw new ProcessingException(e.getMessage());
        }
        return methodsByFileAndVersion;
    }


    private void walkVersions(Git git, List<Version> versions) throws IOException, ProcessingException {
        PmdAnalysis pmdAnalysis = null;
        try (Repository repo = git.getRepository()) {
            for (int i = 0; i < versions.size(); i++) {
                Version previous = i > 0 ? versions.get(i - 1) : null;
                Version current = versions.get(i);
                List<Commit> commits = current.getCommits();
                for (Commit commit : commits) {
                    PmdAnalysis result = analyzeCommit(repo, commit, current, previous);
                    if (result != null) {
                        pmdAnalysis = result;
                    }
                }
            }
            if (pmdAnalysis != null) {
                AbstractControllerFactory<PmdAnalysis, Map<String, List<CodeSmell>>> pmdFileAnalyzerFactory = new PmdFileAnalyzerFactory();
                Map<String, List<CodeSmell>> smells = pmdFileAnalyzerFactory.process(pmdAnalysis);
                computeMethodCodeSmell(smells);
            } else {
                throw new ProcessingException("An error occurred while walking versions");
            }
        }
    }

    private PmdAnalysis analyzeCommit(Repository repository, Commit commit, Version current, Version previous) throws IOException, ProcessingException {
        ObjectId commitId = repository.resolve(commit.id()); // Parse commit id into ObjectId
        PmdAnalysis pmdAnalysis = null;
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
                    List<Method> methods;
                    if (path.endsWith(".java") && !path.endsWith("package-info.java") && commit.changes() != null) {
                        touched = fileIsTouchedBy(commit, path);
                        if (!touched) {
                            manageUntouched(previous, current, path);
                            continue;
                        }

                        String content = getClassContent(repository, treeWalk);
                        methods = computeMetrics(path, content, commit);
                        methodsByFileAndVersion.put(new MethodsKey(current, path), methods);
                        pmdAnalysis = collectContentForSmellComputation(current, path, content);
                    }
                }
            }
            return pmdAnalysis;
        }
    }

    private void manageUntouched(Version previous, Version current, String path) {
        if (methodsByFileAndVersion.containsKey(new MethodsKey(current, path))) {
            return;
        }
        List<Method> methods = previous != null ? methodsByFileAndVersion.get(new MethodsKey(previous, path)) : new ArrayList<>();
        if (methods!= null && !methods.isEmpty()) {
            methodsByFileAndVersion.put(new MethodsKey(current, path), methods);
        }
    }

    private List<Method> computeMetrics(String path, String content, Commit commit) throws ProcessingException {
        AbstractControllerFactory<ParserContext, List<Method>> parserFactory = new JavaMetricParserFactory();
         return parserFactory.process(new ParserContext(content, path, commit));
    }

    private PmdAnalysis collectContentForSmellComputation(Version version, String path, String content) throws ProcessingException {
        AbstractControllerFactory<PmdFileCollectorContext, PmdAnalysis> pmdFactory = new PmdFileCollectorFactory();
        return pmdFactory.process(new PmdFileCollectorContext(version, path, content));
    }

    private void computeMethodCodeSmell(Map<String, List<CodeSmell>> smells) {
        methodsByFileAndVersion.forEach((key, methods) -> {
            String pmdKey = key.version().getName() + "_" + key.path();
            List<CodeSmell> codeSmells = smells.get(pmdKey);
            if (codeSmells != null && !codeSmells.isEmpty()) {
                mapMethodsAndSmells(codeSmells, methods);
            }
        });
    }

    private void mapMethodsAndSmells(List<CodeSmell> smells, List<Method> methods) {
        for (Method method : methods) {
            for (CodeSmell smell : smells) {
                if (smell.getStartLine() >= method.getStartLine() && smell.getStartLine() <= method.getEndLine() &&  smell.getEndLine() >= method.getStartLine() && smell.getEndLine() <= method.getEndLine()) {
                    method.getMetrics().setCodeSmellCounter(method.getMetrics().getCodeSmellCounter() + 1);
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
        for (Change c : commit.changes()) {
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
