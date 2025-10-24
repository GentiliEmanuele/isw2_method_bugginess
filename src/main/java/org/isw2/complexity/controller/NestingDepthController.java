package org.isw2.complexity.controller;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

public class NestingDepthController extends TreeScanner<Void, Integer> {
    private int maxDepth = 0;

    @Override
    public Void visitIf(IfTree node, Integer depth) {
        updateDepth(depth);
        super.visitIf(node, depth + 1);
        return null;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Integer depth) {
        updateDepth(depth);
        super.visitForLoop(node, depth + 1);
        return null;
    }

    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Integer depth) {
        updateDepth(depth);
        return super.visitEnhancedForLoop(node, depth + 1);
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Integer depth) {
        updateDepth(depth);
        super.visitWhileLoop(node, depth + 1);
        return null;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Integer depth) {
        updateDepth(depth);
        super.visitDoWhileLoop(node, depth + 1);
        return null;
    }

    @Override
    public Void visitSwitch(SwitchTree node, Integer depth) {
        updateDepth(depth);
        return super.visitSwitch(node, depth + 1);
    }

    @Override
    public Void visitCase(CaseTree node, Integer depth) {
        updateDepth(depth);
        return super.visitCase(node, depth + 1);
    }

    @Override
    public Void visitBreak(BreakTree node, Integer depth) {
        updateDepth(depth);
        return super.visitBreak(node, depth + 1);
    }

    @Override
    public Void visitContinue(ContinueTree node, Integer depth) {
        updateDepth(depth);
        return super.visitContinue(node, depth + 1);
    }

    @Override
    public Void visitThrow(ThrowTree node, Integer depth) {
        updateDepth(depth);
        return super.visitThrow(node, depth + 1);
    }

    @Override
    public Void visitBlock(BlockTree node, Integer depth) {
        super.visitBlock(node, depth);  // no increment
        return null;
    }

    private void updateDepth(int depth) {
        maxDepth = Math.max(maxDepth, depth + 1);
    }

    public static int computeNestingDepth(MethodTree methodTree, CompilationUnitTree compilationUnitTree, JavacTask javacTask) {
        NestingDepthController nestingDepthScanner = new NestingDepthController();
        if (methodTree.getBody() != null) {
            Trees trees = Trees.instance(javacTask);
            TreePath methodPath = trees.getPath(compilationUnitTree, methodTree);
            nestingDepthScanner.scan(methodPath, 0);
        }
        return nestingDepthScanner.maxDepth;
    }
}
