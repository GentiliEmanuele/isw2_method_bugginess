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
import org.isw2.factory.ExecutionContext;
import org.isw2.metrics.controller.context.JavaMetricParserContext;
import org.isw2.metrics.controller.context.VisitReturn;

import javax.tools.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JavaMetricParser implements Controller {
    // Get access to JavaCompiler
    private final JavaCompiler compiler;
    /* We need an appropriate JavaFileManager instance and an appropriate collection JavaFileObject instances to do
    this. */
    private final StandardJavaFileManager fileManager;

    private String className = "";
    private final List<Method> methods;

    public JavaMetricParser() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.fileManager = this.compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        methods = new ArrayList<>();
    }

    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof JavaMetricParserContext(String content, String path))) {
            throw new ProcessingException("Context is not a JavaMetricParserContext");
        }

        // Create a virtual file in memory
        JavaFileObject fileObject = new SimpleJavaFileObject(URI.create("string:///" + path), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return content;
            }
        };

        JavacTask javacTask = (JavacTask) compiler.getTask(null, fileManager, null, null, null, List.of(fileObject));
        try {
            Iterable<? extends CompilationUnitTree> compilationUnitTrees = javacTask.parse();
            Trees trees = Trees.instance(javacTask);
            parseCompilationUnitTree(compilationUnitTrees, trees);
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void cleanMethodsList() {
        methods.clear();
    }

    private void parseCompilationUnitTree(Iterable<? extends CompilationUnitTree> compilationUnitTrees, Trees trees) {
        for (CompilationUnitTree compilationUnitTree : compilationUnitTrees) {
            parseTrees(compilationUnitTree, trees);
        }
    }

    private void parseTrees(CompilationUnitTree compilationUnitTree, Trees trees) {
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
}
