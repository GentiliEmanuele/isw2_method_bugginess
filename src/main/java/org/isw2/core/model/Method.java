package org.isw2.core.model;

import org.isw2.metrics.model.ComplexityMetrics;
import org.isw2.git.model.Author;
import org.isw2.metrics.model.ChangesMetrics;
import org.isw2.git.model.Commit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Method {
    private String className;
    private String signature;
    private final ComplexityMetrics metrics;
    private final ChangesMetrics changesMetrics;
    private int startLine;
    private int endLine;
    private List<Commit> touchedBy;
    private Set<Author> authors;
    private boolean buggy = false;

    public Method() {
        metrics = new ComplexityMetrics();
        changesMetrics = new ChangesMetrics();
        authors = new HashSet<>();
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

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public int tryToAddAuthor(Author author) {
        authors.add(author);
        return authors.size();
    }

    public boolean getBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }
}
