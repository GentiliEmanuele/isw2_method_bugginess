package org.isw2.metrics.complexity.model;

public class CodeSmell {
    private final int startLine;
    private final int endLine;

    public CodeSmell(int startLine, int endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }
}
