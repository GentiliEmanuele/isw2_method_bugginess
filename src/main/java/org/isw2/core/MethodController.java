package org.isw2.core;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import org.isw2.changes.model.Commit;
import org.isw2.changes.model.Version;
import org.isw2.complexity.controller.ComputeComplexityMetrics;
import org.isw2.complexity.model.Method;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MethodController {

    private int counterMethod = 0;
    private int counterClass = 0;
    private int counterFile = 0;
    Map<Version, List<Method>> methodsByVersion = new HashMap<>();
    List<Method> methods = new ArrayList<>();
    ComputeComplexityMetrics computeComplexityMetrics = new ComputeComplexityMetrics();
    private String className = "";


    public void getAllMethodByProject(String projectPath, List<Commit> commits) {
        // Get access to JavaCompiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        /* We need an appropriate JavaFileManager instance and an appropriate collection JavaFileObject instances to do
        this. */
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        Path startPath = Paths.get(projectPath);
        Logger logger = Logger.getLogger(getClass().getName());
        try (Stream<Path> paths = Files.walk(startPath)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java")) // filter only file .java
                    .filter(path -> !path.toString().endsWith("package-info.java"))
                    .forEach(path -> {
                        try {
                            getAllMethodByClass(compiler, fileManager, path.toString(), commits);
                        } catch (IOException e) {
                            System.exit(1);
                        }
                    });
        } catch (IOException e) {
            System.exit(1);
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("methods: %s", counterMethod));
            logger.info(String.format("class: %s", counterClass));
            logger.info(String.format("file: %s", counterFile));
        }

    }

    private void getAllMethodByClass(JavaCompiler compiler, StandardJavaFileManager fileManager, String classPath, List<Commit> commits) throws IOException {
        // Once we've got this, we can the use it (instance of JavaFileManager) to access the file that we want to process
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(List.of(new File(classPath)));
        // Once we have these, we can then process our files
        JavacTask javacTask = (JavacTask) compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        Iterable<? extends CompilationUnitTree> compilationUnitTrees = javacTask.parse();
        Trees trees = Trees.instance(javacTask);
        counterFile++;
        for (CompilationUnitTree compilationUnitTree : compilationUnitTrees) {
            for (Tree tree : compilationUnitTree.getTypeDecls()) {
                tree.accept(new TreeScanner<>() {
                    @Override
                    public Object visitClass(ClassTree classTree, Object o) {
                        className = classTree.getSimpleName().toString();
                        counterClass++;
                        return super.visitClass(classTree, o);
                    }

                    @Override
                    public Object visitMethod(MethodTree methodTree, Object o) {
                        Method method = new Method();
                        method.setClassName(className);
                        method.getMetrics().setLinesOfCode(computeComplexityMetrics.computeLinesOfCode(methodTree));
                        method.getMetrics().setCyclomaticComplexity(computeComplexityMetrics.computeCyclomaticComplexity(methodTree));
                        method.getMetrics().setStatementsCount(computeComplexityMetrics.computeStatementsCount(methodTree));
                        method.getMetrics().setCognitiveComplexity(computeComplexityMetrics.computeCognitiveComplexity(methodTree, compilationUnitTree, javacTask));
                        method.getMetrics().setHalsteadComplexity(computeComplexityMetrics.computeHalstedComplexity(methodTree));
                        method.getMetrics().setNestingDepth(computeComplexityMetrics.computeNestingDepth(methodTree, compilationUnitTree, javacTask));
                        method.getMetrics().setNumberOfBranchesAndDecisionPoint(method.getMetrics().getCyclomaticComplexity() - 1);
                        method.getMetrics().setParameterCount(getParametersCounter(methodTree));
                        long startPosition = trees.getSourcePositions().getStartPosition(compilationUnitTree, methodTree);
                        long endPosition = trees.getSourcePositions().getEndPosition(compilationUnitTree, methodTree);
                        int startLine = (int) compilationUnitTree.getLineMap().getLineNumber(startPosition);
                        int endLine = (int) compilationUnitTree.getLineMap().getLineNumber(endPosition);
                        method.setStartLine(startLine);
                        method.setEndLine(endLine);
                        methods.add(method);
                        debugPrint(method);
                        counterMethod++;
                        return super.visitMethod(methodTree, o);
                    }
                }, null);
            }
        }
    }

    public List<Method> getMethods() {
        return methods;
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

    private void debugPrint(Method method) {
        System.out.println(method.getClassName() + " "
                                + method.getSignature() + " "
                                + method.getMetrics().getLinesOfCode() + " "
                                + method.getMetrics().getCyclomaticComplexity() + " "
                                + method.getMetrics().getStatementsCount() + " "
                                + method.getMetrics().getCognitiveComplexity() + " "
                                + method.getMetrics().getHalsteadComplexity().toString() + " "
                                + method.getMetrics().getNestingDepth() + " "
                                + method.getMetrics().getNumberOfBranchesAndDecisionPoint() + " "
                                + method.getMetrics().getParameterCount() + " "
                                + method.getStartLine() + " "
                                + method.getEndLine() + " "
                                + method.getTouchedBy()
        );
    }
}