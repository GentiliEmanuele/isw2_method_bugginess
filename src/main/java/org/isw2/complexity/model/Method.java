package org.isw2.complexity.model;

import org.isw2.changes.model.ChangesMetrics;
import org.isw2.changes.model.Commit;

import java.util.List;

public class Method {
    private String className;
    private String signature;
    private final ComplexityMetrics metrics;
    private final ChangesMetrics changesMetrics;
    private int startLine;
    private int endLine;
    private List<Commit> touchedBy;

    public Method() {
        metrics = new ComplexityMetrics();
        changesMetrics = new ChangesMetrics();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public ComplexityMetrics getMetrics() {
        return metrics;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public List<Commit> getTouchedBy() {
        return touchedBy;
    }

    public void setTouchedBy(List<Commit> touchedBy) {
        this.touchedBy = touchedBy;
    }

    public ChangesMetrics getChangesMetrics() {
        return changesMetrics;
    }
}
