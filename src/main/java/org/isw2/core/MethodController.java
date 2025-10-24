package org.isw2.complexity.controller;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MethodController {

    private int counterMethod = 0;
    private int counterClass = 0;
    private int counterFile = 0;
    List<Method> methods = new ArrayList<>();
    ComputeMetrics computeMetrics = new ComputeMetrics();
    private String className = "";


    public void getAllMethodByProject(String projectPath) {
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
                            getAllMethodByClass(compiler, fileManager, path.toString());
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

    private void getAllMethodByClass(JavaCompiler compiler, StandardJavaFileManager fileManager, String classPath) throws IOException {
        // Once we've got this, we can the use it (instance of JavaFileManager) to access the file that we want to process
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(List.of(new File(classPath)));
        // Once we have these, we can then process our files
        JavacTask javacTask = (JavacTask) compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        Iterable<? extends CompilationUnitTree> compilationUnitTrees = javacTask.parse();
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
                        method.setSignature(getReturnValue(methodTree) + " " + getMethodName(methodTree) + "(" + getMethodParameters(methodTree) + ")");
                        method.getMetrics().setLinesOfCode(computeMetrics.computeLinesOfCode(methodTree));
                        method.getMetrics().setCyclomaticComplexity(computeMetrics.computeCyclomaticComplexity(methodTree));
                        method.getMetrics().setStatementsCount(computeMetrics.computeStatementsCount(methodTree));
                        method.getMetrics().setCognitiveComplexity(computeMetrics.computeCognitiveComplexity(methodTree, compilationUnitTree, javacTask));
                        method.getMetrics().setHalsteadComplexity(computeMetrics.computeHalstedComplexity(methodTree));
                        method.getMetrics().setNestingDepth(computeMetrics.computeNestingDepth(methodTree, compilationUnitTree, javacTask));
                        method.getMetrics().setNumberOfBranchesAndDecisionPoint(method.getMetrics().getCyclomaticComplexity() - 1);
                        method.getMetrics().setParameterCount(getParametersCounter(methodTree));
                        debugPrint(method);
                        counterMethod++;
                        return super.visitMethod(methodTree, o);
                    }
                }, null);
            }
        }
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

    private String getMethodName(MethodTree methodTree) {
        return methodTree.getName().toString();
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
                                + method.getMetrics().getParameterCount());
    }
}