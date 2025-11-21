package org.isw2.core.controller;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
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
import org.isw2.metrics.changes.controller.ComputeChangesMetrics;
import org.isw2.git.model.Change;
import org.isw2.git.model.Commit;
import org.isw2.jira.model.Version;
import org.isw2.metrics.complexity.controller.ComputeComplexityMetrics;
import org.isw2.core.model.Method;
import org.isw2.git.controller.GitController;
import org.isw2.metrics.complexity.controller.VisitMethod;
import org.isw2.metrics.complexity.controller.context.VisitReturn;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MapCommitsAndMethods implements Controller {

    private final Map<Version, List<Method>> methodsByVersion = new HashMap<>();
    private String className = "";
    private final ComputeComplexityMetrics computeComplexityMetrics = new ComputeComplexityMetrics();
    private final Map<String, List<Method>> methodCache = new HashMap<>();
    private final Map<Version, List<FileClass>> fileClassByVersion = new HashMap<>();
    private final ComputeChangesMetrics computeChangesMetrics = new ComputeChangesMetrics();

    // Get access to JavaCompiler
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    /* We need an appropriate JavaFileManager instance and an appropriate collection JavaFileObject instances to do
    this. */
    private final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);

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
            getBasicInfo(
                    getGit(gitController),
                    versions
            );
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    public Map<Version, List<Method>> getMethodsByVersion() {
        return methodsByVersion;
    }

    private Git getGit(Controller gitController) throws ProcessingException {
        if (gitController instanceof GitController controller) {
            return controller.getGit();
        } else {
            throw new ProcessingException("Processing error was occurred");
        }
    }

    private void getBasicInfo(Git git, List<Version> versions) throws IOException, ProcessingException {
        int versionSize = versions.size();
        int processedVersion = 0;
        try (Repository repo = git.getRepository()) {
            for (Version version : versions) {
                int percentVersion = (100 * processedVersion) / versionSize;
                List<Commit> commits = version.getCommits();
                int commitSize = commits.size();
                int processedCommits = 0;
                Map<String, Method> methodsMap = new HashMap<>();
                List<FileClass> fileClassList = new ArrayList<>();
                for (Commit commit : commits) {
                    analyzeCommit(repo, commit, methodsMap, fileClassList);
                    processedCommits++;
                    int percentCommit = (100 * processedCommits) / commitSize;
                    System.out.print("\rMap commits and methods, progress of the versions " + percentVersion + "%" + " commits progress " + percentCommit + "%");
                }
                methodsByVersion.put(version, new ArrayList<>(methodsMap.values()));
                fileClassByVersion.put(version, new ArrayList<>(fileClassList));
                processedVersion++;
            }
        }
        System.out.println();
    }

    public Map<Version, List<FileClass>> getFileClassByVersion() {
        return fileClassByVersion;
    }

    private void analyzeCommit(Repository repository, Commit commit, Map<String, Method> methodsMap, List<FileClass> fileClassList) throws IOException, ProcessingException {
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
                        touched = classIsTouchedBy(commit, path);
                        if (!touched) {
                            getInfoFromCache(path, methodsMap);
                            continue;
                        }

                        String content = getClassContent(repository, treeWalk);
                        FileClass fileClass = new FileClass(path, content);
                        analyzeJavaSource(content, path, commit, methodsMap, fileClass.getMethods());
                        methodCache.put(path, new ArrayList<>(fileClass.getMethods()));
                        fileClassList.add(fileClass);
                    }
                }
            }
        }
    }

    private void getInfoFromCache(String path, Map<String, Method> methodsMap) {
        List<Method> cachedMethods = methodCache.get(path);
        if (cachedMethods != null) {
            for (Method m : cachedMethods) {
                String key = m.getClassName() + "#" + m.getSignature();
                methodsMap.put(key, m);
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

    private void analyzeJavaSource(String content, String path, Commit currentCommit, Map<String, Method> methodsMap, List<Method> methodsByFile) throws IOException, ProcessingException {
        // Create a virtual file in memory
        JavaFileObject fileObject = new SimpleJavaFileObject(URI.create("string:///" + path), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return content;
            }
        };

        JavacTask javacTask = (JavacTask) compiler.getTask(null, fileManager, null, null, null, List.of(fileObject));
        Iterable<? extends CompilationUnitTree> compilationUnitTrees = javacTask.parse();
        Trees trees = Trees.instance(javacTask);
        for (CompilationUnitTree compilationUnitTree : compilationUnitTrees) {
            for (Tree tree : compilationUnitTree.getTypeDecls()) {
                tree.accept(new TreeScanner<>() {
                    @Override
                    public Object visitClass(ClassTree classTree, Object o) {
                        className = sanitize(classTree.getSimpleName().toString());
                        return super.visitClass(classTree, o);
                    }

                    @Override
                    public Object visitMethod(MethodTree methodTree, Object o) {
                        String methodKey = className + "#" + getMethodName(methodTree) + "(" + getMethodParameters(methodTree) + ")";
                        Method method = methodsMap.get(methodKey);
                        if (method == null) {
                            method = new Method();
                            method.setClassName(className);
                            method.setSignature(getReturnValue(methodTree) + " " + getMethodName(methodTree) + "(" + getMethodParameters(methodTree) + ")");
                            methodsMap.put(methodKey, method);
                        }

                        long startPosition = trees.getSourcePositions().getStartPosition(compilationUnitTree, methodTree);
                        long endPosition = trees.getSourcePositions().getEndPosition(compilationUnitTree, methodTree);
                        int startLine = (int) compilationUnitTree.getLineMap().getLineNumber(startPosition);
                        int endLine = (int) compilationUnitTree.getLineMap().getLineNumber(endPosition);
                        method.setStartLine(startLine);
                        method.setEndLine(endLine);
                        method.getMetrics().setLinesOfCode(endLine - startLine);
                        VisitReturn ret = VisitMethod.execute(methodTree);
                        method.getMetrics().setCyclomaticComplexity(ret.cyclomaticComplexity());
                        method.getMetrics().setStatementsCount(ret.statementCount());
                        method.getMetrics().setCognitiveComplexity(ret.cognitiveComplexity());
                        method.getMetrics().setHalsteadComplexity(computeComplexityMetrics.computeHalstedComplexity(methodTree));
                        method.getMetrics().setNestingDepth(computeComplexityMetrics.computeNestingDepth(methodTree, compilationUnitTree, javacTask));
                        method.getMetrics().setNumberOfBranchesAndDecisionPoint(method.getMetrics().getCyclomaticComplexity() - 1);
                        method.getMetrics().setParameterCount(getParametersCounter(methodTree));

                        if (methodIsToucheBy(method, currentCommit, path)) {
                            computeChangesMetrics.computeMethodHistories(method);
                            computeChangesMetrics.computeAuthors(method,  currentCommit);
                            computeChangesMetrics.computeStmtAdded(method);
                            computeChangesMetrics.computeStmtDeleted(method);
                        }
                        methodsByFile.add(method);
                        return super.visitMethod(methodTree, o);
                    }
                }, null);
            }
        }
    }

    private boolean methodIsToucheBy(Method method, Commit commit, String classPath) {
        boolean touched = false;
        List<Change> changes = commit.getChanges();
        if (changes == null) return false;
        for (Change change : changes) {
            touched = isTouchedByAdd(change, classPath) || methodIsTouchedByModify(change, classPath, method);
            if (touched) {
                if (method.getTouchedBy() == null) {
                    method.setTouchedBy(new ArrayList<>());
                }
                method.getTouchedBy().add(commit);
            }
        }
        return touched;
    }

    private boolean classIsTouchedBy(Commit commit, String classPath) {
        for (Change c : commit.getChanges()) {
            if (isTouchedByAdd(c, classPath) || classIsTouchedByModify(c, classPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTouchedByAdd (Change change, String classPath) {
        return change.getType().equals("ADD") && change.getNewPath().equals(classPath);
    }

    private boolean methodIsTouchedByModify(Change change, String classPath, Method method) {
        return change.getType().equals("MODIFY") && change.getOldPath().equals(classPath) && change.getOldStart() == method.getStartLine() && change.getOldEnd() == method.getEndLine();
    }

    private boolean classIsTouchedByModify(Change change, String classPath) {
        return change.getType().equals("MODIFY") && change.getOldPath().equals(classPath);
    }

    private int getParametersCounter(MethodTree methodTree) {
        String all = getMethodParameters(methodTree);
        String[] listAll = all.split(",");
        int counter = 0;
        for (String s : listAll) {
            if (!s.trim().equals("void")) {
                counter ++;
            }
        }
        return counter;
    }

    private String sanitize(String input) {
        if (input == null) return "";

        return input.replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String getMethodName(MethodTree methodTree) {
        return sanitize(methodTree.getName().toString());
    }

    private String getMethodParameters(MethodTree methodTree) {
        if (methodTree.getParameters().isEmpty()) {
            return "void";
        } else {
            return sanitize(methodTree.getParameters().toString());
        }
    }

    private String getReturnValue(MethodTree methodTree) {
        if (methodTree.getReturnType() != null) {
            return sanitize(methodTree.getReturnType().toString());
        } else {
            return "void";
        }
    }
}
