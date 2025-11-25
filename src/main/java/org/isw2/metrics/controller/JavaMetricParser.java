package org.isw2.metrics.controller;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import org.isw2.core.model.Method;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.git.model.Change;
import org.isw2.git.model.Commit;
import org.isw2.metrics.controller.context.ParserContext;
import org.isw2.metrics.controller.context.VisitReturn;

import javax.tools.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaMetricParser implements Controller<ParserContext, List<Method>> {

    // Get access to JavaCompiler
    private final JavaCompiler compiler;
    /* We need an appropriate JavaFileManager instance and an appropriate collection JavaFileObject instances to do
    this. */
    private final StandardJavaFileManager fileManager;

    private String className;

    JavaMetricParser() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.fileManager = this.compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
    }

    private static class SingletonHelper {
        private static final JavaMetricParser INSTANCE = new JavaMetricParser();
    }

    public static JavaMetricParser getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public List<Method> execute(ParserContext context) throws ProcessingException {
        List<Method> methods = new ArrayList<>();
        // Create a virtual file in memory
        JavaFileObject fileObject = new SimpleJavaFileObject(URI.create("string:///" + context.filePath()), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return context.content();
            }
        };

        JavacTask javacTask = (JavacTask) compiler.getTask(null, fileManager, null, null, null, List.of(fileObject));
        try {
            Iterable<? extends CompilationUnitTree> compilationUnitTrees = javacTask.parse();
            Trees trees = Trees.instance(javacTask);
            parseCompilationUnitTree(compilationUnitTrees, trees, methods, context.commit(), context.filePath());
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
        return methods;
    }

    private void parseCompilationUnitTree(Iterable<? extends CompilationUnitTree> compilationUnitTrees, Trees trees, List<Method> outMethods, Commit commit, String path) {
        for (CompilationUnitTree compilationUnitTree : compilationUnitTrees) {
            parseTrees(compilationUnitTree, trees, outMethods, commit, path);
        }
    }

    private void parseTrees(CompilationUnitTree compilationUnitTree, Trees trees, List<Method> methods, Commit commit, String path) {
        for (Tree tree : compilationUnitTree.getTypeDecls()) {
            tree.accept(new TreeScanner<>() {
                @Override
                public Object visitClass(ClassTree classTree, Object o) {
                    className = sanitize(classTree.getSimpleName().toString());
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
                    method.getMetrics().setLinesOfCode(endLine - startLine);
                    VisitReturn ret = VisitMethod.execute(methodTree);
                    method.getMetrics().setCyclomaticComplexity(ret.cyclomaticComplexity());
                    method.getMetrics().setStatementsCount(ret.statementCount());
                    method.getMetrics().setCognitiveComplexity(ret.cognitiveComplexity());
                    method.getMetrics().setHalsteadComplexity(ret.hc());
                    method.getMetrics().setNestingDepth(ret.nestingDepth());
                    method.getMetrics().setNumberOfBranchesAndDecisionPoint(ret.cyclomaticComplexity() - 1);
                    method.getMetrics().setParameterCount(getParametersCounter(methodTree));

                    methodIsToucheBy(method, commit, path);

                    methods.add(method);
                    return super.visitMethod(methodTree, o);
                }
            }, null);
        }
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

    private void methodIsToucheBy(Method method, Commit commit, String classPath) {
        List<Change> changes = commit.getChanges();
        if (changes == null) return;
        for (Change change : changes) {
            boolean touched = isTouchedByAdd(change, classPath) || methodIsTouchedByModify(change, classPath, method);
            if (touched) {
                if (method.getTouchedBy() == null) {
                    method.setTouchedBy(new ArrayList<>());
                }
                method.getTouchedBy().add(commit);
            }
        }
    }

    private boolean isTouchedByAdd (Change change, String classPath) {
        return change.getType().equals("ADD") && change.getNewPath().equals(classPath);
    }

    private boolean methodIsTouchedByModify(Change change, String classPath, Method method) {
        return change.getType().equals("MODIFY") && change.getOldPath().equals(classPath) && change.getOldStart() == method.getStartLine() && change.getOldEnd() == method.getEndLine();
    }
}
