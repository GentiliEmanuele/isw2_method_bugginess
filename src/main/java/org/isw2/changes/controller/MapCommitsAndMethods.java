package org.isw2.changes.controller;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.isw2.changes.model.Change;
import org.isw2.changes.model.Commit;
import org.isw2.changes.model.Version;
import org.isw2.complexity.controller.ComputeComplexityMetrics;
import org.isw2.complexity.model.Method;
import org.isw2.core.boundary.GitController;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MapCommitsAndMethods {

    private Map<Version, List<Method>> methodsByVersion = new HashMap<>();
    private String className = "";
    private List<Method> methods;
    private final ComputeComplexityMetrics computeComplexityMetrics = new ComputeComplexityMetrics();
    private ControllerChangesMetrics controllerChangesMetrics = new ControllerChangesMetrics();

    public Map<Version, List<Method>> getBasicInfo(String projectPath, List<Version> versions, GitController gitController) throws GitAPIException {
        for (Version version : versions) {
            List<Commit> commits = version.getCommits();
            methods = new ArrayList<>();
            for (Commit commit : commits) {
                System.out.println(commit.getId() + " of version " + version.getId() + " last version is " +  versions.getLast());
                gitController.checkout(commit.getId());
                mapCommitsAndMethods(projectPath, version, versions.getFirst(), commits);
            }
            methodsByVersion.put(version, methods);
        }
        return methodsByVersion;
    }

    private void mapCommitsAndMethods(String projectPath,  Version current, Version first, List<Commit> commits) {
        // Get access to JavaCompiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        /* We need an appropriate JavaFileManager instance and an appropriate collection JavaFileObject instances to do
        this. */
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        Path startPath = Paths.get(projectPath);
        try (Stream<Path> paths = Files.walk(startPath)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java")) // filter only file .java
                    .filter(path -> !path.toString().endsWith("package-info.java"))
                    .forEach(path -> {
                        try {
                            getMethodsHistory(compiler, fileManager, String.valueOf(path), current, first, commits);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            System.exit(1);
        }
    }

    private void getMethodsHistory(JavaCompiler compiler, StandardJavaFileManager fileManager, String classPath, Version current, Version first, List<Commit> commits) throws IOException {
        // Once we've got this, we can the use it (instance of JavaFileManager) to access the file that we want to process
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(List.of(new File(classPath)));
        // Once we have these, we can then process our files
        JavacTask javacTask = (JavacTask) compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        Iterable<? extends CompilationUnitTree> compilationUnitTrees = javacTask.parse();
        Trees trees = Trees.instance(javacTask);
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
                        Method method = new Method();
                        method.setClassName(className);
                        method.setSignature(getReturnValue(methodTree) + " " + getMethodName(methodTree) + "(" + getMethodParameters(methodTree) + ")");
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
                        method.getMetrics().setNumberOfBranchesAndDecisionPoint(method.getMetrics().getCyclomaticComplexity() - 1);
                        method.getMetrics().setParameterCount(getParametersCounter(methodTree));
                        isToucheBy(method, commits, classPath);
                        method.getChangesMetrics().setMethodHistories(controllerChangesMetrics.wrapperComputeMethodHistories(method, first, current));
                        method.getChangesMetrics().setMethodHistories(controllerChangesMetrics.wrapperComputeAuthors(method, first, current));
                        methods.add(method);
                        return super.visitMethod(methodTree, o);
                    }
                }, null);
            }
        }
    }

    // TODO(Check if there are cases of changes of type DELETE, RENAME or COPY
    private void isToucheBy(Method method, List<Commit> commits, String classPath) {
        List<Commit> touchedBy = new ArrayList<>();
        String root = "/home/emanuele/isw2/temp/BOOKKEEPER/";
        commits.forEach(commit -> {
            List<Change> changes = commit.getChanges();
            if (changes != null) {
                for (Change change : changes) {
                    if (change.getType().equals("ADD") && change.getNewPath().equals(classPath.replace(root, ""))) {
                        touchedBy.add(commit);
                    }
                    if (change.getType().equals("MODIFY") && change.getOldPath().equals(classPath.replace(root, "")) && change.getOldStart() == method.getStartLine() && change.getOldEnd() == method.getEndLine()) {
                        touchedBy.add(commit);
                    }
                }
            }
        });
        method.setTouchedBy(touchedBy);
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
