package org.isw2.dataset.metrics;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.metrics.controller.VisitMethod;
import org.isw2.dataset.metrics.controller.context.VisitReturn;

import java.util.Map;

public class MethodParserScanner extends TreeScanner<Object, String> {
    private final CompilationUnitTree compilationUnitTree;
    private final Trees trees;
    private final Map<MethodKey, Method> methods;
    private final String path;

    public MethodParserScanner(CompilationUnitTree compilationUnitTree, Trees trees, Map<MethodKey, Method> methods, String path) {
        this.compilationUnitTree = compilationUnitTree;
        this.trees = trees;
        this.methods = methods;
        this.path = path;
    }

    @Override
    public Object visitClass(ClassTree classTree, String parentName) {
        String simpleName;
        String currentClassName;

        // Check if the class is anonymous
        if (classTree.getSimpleName().isEmpty()) {
            return null;
        } else {
            // In this case is a normal class
            simpleName = sanitize(classTree.getSimpleName().toString());
        }

        // Build the whole name (Parent.Child)
        if (parentName != null && !parentName.isEmpty()) {
            String separator = classTree.getSimpleName().isEmpty() ? "$" : ".";
            currentClassName = parentName + separator + simpleName;
        } else {
            currentClassName = simpleName;
        }

        return super.visitClass(classTree, currentClassName);
    }

    @Override
    public Object visitMethod(MethodTree methodTree, String currentClassName) {
        if (methodTree.getBody() == null) {
            return super.visitMethod(methodTree, currentClassName);
        }
        Method method = new Method();

        String modifiers = getModifiers(methodTree).isEmpty() ? "" : getModifiers(methodTree) + " ";
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        method.setMethodKey(new MethodKey(cleanPath, currentClassName, modifiers + getReturnValue(methodTree) + " " + getMethodName(methodTree) + "(" + getMethodParameters(methodTree) + ")"));

        long startPosition = trees.getSourcePositions().getStartPosition(compilationUnitTree, methodTree);
        long endPosition = trees.getSourcePositions().getEndPosition(compilationUnitTree, methodTree);
        int startLine = (int) compilationUnitTree.getLineMap().getLineNumber(startPosition);
        int endLine = (int) compilationUnitTree.getLineMap().getLineNumber(endPosition);
        method.setStartLine(startLine);
        method.setEndLine(endLine);
        method.getMetrics().setLinesOfCode((endLine - startLine) + 1);
        VisitReturn ret = VisitMethod.execute(methodTree);
        method.getMetrics().setCyclomaticComplexity(ret.cyclomaticComplexity());
        method.getMetrics().setStatementsCount(ret.statementCount());
        method.getMetrics().setCognitiveComplexity(ret.cognitiveComplexity());
        method.getMetrics().setHalsteadComplexity(ret.hc());
        method.getMetrics().setNestingDepth(ret.nestingDepth());
        method.getMetrics().setNumberOfBranchesAndDecisionPoint(ret.cyclomaticComplexity() - 1);
        method.getMetrics().setParameterCount(getParametersCounter(methodTree));

        methods.put(method.getMethodKey(), method);
        return super.visitMethod(methodTree, currentClassName);
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
        return methodTree.getParameters().size();
    }

    private String getModifiers(MethodTree methodTree) {
        if (methodTree.getModifiers() == null) {
            return "";
        }
        return sanitize(methodTree.getModifiers().getFlags().toString()).replace("[", "")
                .replace("]", "")
                .replace(",", "");
    }
}


