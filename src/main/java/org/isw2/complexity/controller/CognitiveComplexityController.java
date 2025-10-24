package org.isw2.complexity.controller;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

public class CognitiveComplexityController extends TreePathScanner<Void, Integer> {

        private int cognitiveComplexity = 0;

        @Override
        public Void visitIf(IfTree node, Integer nestingLevel) {
            cognitiveComplexity += 1 + nestingLevel;
            return super.visitIf(node, nestingLevel + 1);
        }

        @Override
        public Void visitForLoop(ForLoopTree node, Integer nestingLevel) {
            cognitiveComplexity += 1 + nestingLevel;
            return super.visitForLoop(node, nestingLevel + 1);
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree node, Integer nestingLevel) {
            cognitiveComplexity += 1 + nestingLevel;
            return super.visitEnhancedForLoop(node, nestingLevel + 1);
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree node, Integer nestingLevel) {
            cognitiveComplexity += 1 + nestingLevel;
            return super.visitWhileLoop(node, nestingLevel + 1);
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree node, Integer nestingLevel) {
            cognitiveComplexity += 1 + nestingLevel;
            return super.visitDoWhileLoop(node, nestingLevel + 1);
        }

        @Override
        public Void visitSwitch(SwitchTree node, Integer nestingLevel) {
            cognitiveComplexity += 1 + nestingLevel;
            return super.visitSwitch(node, nestingLevel + 1);
        }

        @Override
        public Void visitCase(CaseTree node, Integer nestingLevel) {
            cognitiveComplexity += 1 + nestingLevel;
            return super.visitCase(node, nestingLevel + 1);
        }

        @Override
        public Void visitReturn(ReturnTree node, Integer nestingLevel) {
            cognitiveComplexity += 1;
            return super.visitReturn(node, nestingLevel);
        }

        @Override
        public Void visitBreak(BreakTree node, Integer nestingLevel) {
            cognitiveComplexity += 1;
            return super.visitBreak(node, nestingLevel);
        }

        @Override
        public Void visitContinue(ContinueTree node, Integer nestingLevel) {
            cognitiveComplexity += 1;
            return super.visitContinue(node, nestingLevel);
        }

        @Override
        public Void visitThrow(ThrowTree node, Integer nestingLevel) {
            cognitiveComplexity += 1;
            return super.visitThrow(node, nestingLevel);
        }


        @Override
        public Void visitMethod(MethodTree node, Integer nestingLevel) {
            super.visitMethod(node, 0);
            return null;
        }

        public static int computeCognitiveComplexity(MethodTree methodTree, CompilationUnitTree compilationUnitTree, JavacTask javacTask) {
            CognitiveComplexityController complexityScanner = new CognitiveComplexityController();
            if (methodTree.getBody() != null) {
                Trees trees = Trees.instance(javacTask);
                TreePath methodPath = trees.getPath(compilationUnitTree, methodTree);
                complexityScanner.scan(methodPath, 0);
            }
            return complexityScanner.cognitiveComplexity;
        }
}
