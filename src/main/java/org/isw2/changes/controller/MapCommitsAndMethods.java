package org.isw2.changes.controller;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.isw2.changes.model.Change;
import org.isw2.changes.model.Commit;
import org.isw2.changes.model.Version;
import org.isw2.complexity.controller.CodeSmellAnalyzer;
import org.isw2.complexity.controller.ComputeComplexityMetrics;
import org.isw2.complexity.model.CodeSmell;
import org.isw2.complexity.model.Method;
import org.isw2.core.boundary.GitController;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapCommitsAndMethods {

    private final Map<Version, List<Method>> methodsByVersion = new HashMap<>();
    private String className = "";
    private final ComputeComplexityMetrics computeComplexityMetrics = new ComputeComplexityMetrics();
    private final Map<String, List<Method>> methodCache = new HashMap<>();
    private final CodeSmellAnalyzer codeSmellAnalyzer = new CodeSmellAnalyzer();

    // Get access to JavaCompiler
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    /* We need an appropriate JavaFileManager instance and an appropriate collection JavaFileObject instances to do
    this. */
    private final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);

    public Map<Version, List<Method>> getBasicInfo(List<Version> versions, GitController gitController) throws GitAPIException, IOException {
        int versionSize = versions.size();
        int processedVersion = 0;
        try (Repository repo = gitController.cloneRepository("BOOKKEEPER").getRepository()) {
            for (Version version : versions) {
                int percentVersion = (100 * processedVersion) / versionSize;
                List<Commit> commits = version.getCommits();
                int commitSize = commits.size();
                int processedCommits = 0;
                Map<String, Method> methodsMap = new HashMap<>();
                for (Commit commit : commits) {
                    // System.out.println(commit.getId() + " of version " + version.getId() + " last version is " +  versions.getLast());
                    analyzeCommit(repo, commit, version, methodsMap);
                    processedCommits++;
                    int percentCommit = (100 * processedCommits) / commitSize;
                    System.out.print("\rMap commits and methods, progress of the versions " + percentVersion + "%" + " commits progress " + percentCommit + "%");
                }
                methodsByVersion.put(version, new ArrayList<>(methodsMap.values()));
                processedVersion++;
            }
        }
        System.out.println();
        return methodsByVersion;
    }

    private void analyzeCommit(Repository repository, Commit commit, Version current, Map<String, Method> methodsMap) throws IOException {
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
                    boolean touched = false;

                    if (!path.endsWith(".java") || path.endsWith("package-info.java")) {
                        continue;
                    }

                    if (commit.getChanges() == null) continue;

                    for (Change c : commit.getChanges()) {
                        if (c.getType().equals("MODIFY") && (c.getNewPath().equals(path) && c.getOldPath().equals(path))) {
                            touched = true;
                            break;
                        } else if (c.getType().equals("ADD") && c.getNewPath().equals(path)) {
                            touched = true;
                            break;
                        }
                    }

                    if (!touched) {
                        List<Method> cachedMethods = methodCache.get(path);
                        if (cachedMethods != null) {
                            for (Method m : cachedMethods) {
                                String key = m.getClassName() + "#" + m.getSignature();
                                methodsMap.put(key, m);
                            }
                        }
                        continue;
                    }

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
                    List<Method> methodByFile = new ArrayList<>();
                    analyzeJavaSource(content, path, commit, methodsMap, methodByFile);
                    methodCache.put(path, new ArrayList<>(methodByFile));
                }
            }
        }
    }

    private void analyzeJavaSource(String content, String path, Commit currentCommit, Map<String, Method> methodsMap, List<Method> methodsByFile) throws IOException {
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
        List<CodeSmell> smells = codeSmellAnalyzer.findSmells(content);
        for (CompilationUnitTree compilationUnitTree : compilationUnitTrees) {
            for (Tree tree : compilationUnitTree.getTypeDecls()) {
                tree.accept(new TreeScanner<>() {
                    @Override
                    public Object visitClass(ClassTree classTree, Object o) {
                        className = classTree.getSimpleName().toString();
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
                        method.getMetrics().setLinesOfCode(computeComplexityMetrics.computeLinesOfCode(methodTree));
                        method.getMetrics().setCyclomaticComplexity(computeComplexityMetrics.computeCyclomaticComplexity(methodTree));
                        method.getMetrics().setStatementsCount(computeComplexityMetrics.computeStatementsCount(methodTree));
                        method.getMetrics().setCognitiveComplexity(computeComplexityMetrics.computeCognitiveComplexity(methodTree, compilationUnitTree, javacTask));
                        method.getMetrics().setHalsteadComplexity(computeComplexityMetrics.computeHalstedComplexity(methodTree));
                        method.getMetrics().setNestingDepth(computeComplexityMetrics.computeNestingDepth(methodTree, compilationUnitTree, javacTask));
                        method.getMetrics().setCodeSmellCounter(computeComplexityMetrics.computeSmellNumber(smells, startLine, endLine));
                        method.getMetrics().setNumberOfBranchesAndDecisionPoint(method.getMetrics().getCyclomaticComplexity() - 1);
                        method.getMetrics().setParameterCount(getParametersCounter(methodTree));

                        if (isToucheBy(method, currentCommit, path)) {
                            method.getChangesMetrics().setMethodHistories(method.getChangesMetrics().getMethodHistories() + 1);
                            // If the author is already registered newAuthorNr = oldAuthorNr
                            int newAuthorsNr = method.tryToAddAuthor(currentCommit.getAuthor());
                            method.getChangesMetrics().setAuthors(newAuthorsNr);
                        }
                        methodsByFile.add(method);
                        return super.visitMethod(methodTree, o);
                    }
                }, null);
            }
        }
    }

    private boolean isToucheBy(Method method, Commit commit, String classPath) {
        boolean touched = false;
        String root = "/home/emanuele/isw2/temp/BOOKKEEPER/";
        List<Change> changes = commit.getChanges();
        if (changes != null) {
            for (Change change : changes) {
                if (change.getType().equals("ADD") && change.getNewPath().equals(classPath.replace(root, ""))) {
                    touched = true;
                    if (method.getTouchedBy() == null) {
                        method.setTouchedBy(new ArrayList<>());
                    }
                    method.getTouchedBy().add(commit);
                    break;
                }
                if (change.getType().equals("MODIFY") && change.getOldPath().equals(classPath.replace(root, "")) && change.getOldStart() == method.getStartLine() && change.getOldEnd() == method.getEndLine()) {
                    touched = true;
                    if (method.getTouchedBy() == null) {
                        method.setTouchedBy(new ArrayList<>());
                    }
                    method.getTouchedBy().add(commit);
                    break;
                }
            }
        }
        return touched;
    }


    private String getMethodName(MethodTree methodTree) {
        return methodTree.getName().toString();
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

    private String getMethodParameters(MethodTree methodTree) {
        if (methodTree.getParameters().isEmpty()) {
            return "void";
        } else {
            return methodTree.getParameters().toString();
        }
    }

    private String getReturnValue(MethodTree methodTree) {
        if (methodTree.getReturnType() != null) {
            return methodTree.getReturnType().toString();
        } else {
            return "void";
        }
    }

}
