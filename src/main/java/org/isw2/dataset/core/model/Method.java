package org.isw2.dataset.core.model;

import org.isw2.dataset.metrics.model.ComplexityMetrics;
import org.isw2.dataset.git.model.Author;
import org.isw2.dataset.metrics.model.ChangesMetrics;
import org.isw2.dataset.git.model.Commit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Method {
    private MethodKey methodKey;
    private ComplexityMetrics metrics;
    private final ChangesMetrics changesMetrics;
    private int startLine;
    private int endLine;
    private List<Commit> touchedBy;
    private Set<Author> authors;
    private final IsBuggy buggy;

    public Method() {
        metrics = new ComplexityMetrics();
        changesMetrics = new ChangesMetrics();
        authors = new HashSet<>();
        touchedBy = new ArrayList<>();
        buggy = new IsBuggy();
    }

    public Method(Method other) {
        this.methodKey = other.methodKey;
        this.startLine = other.startLine;
        this.endLine = other.endLine;
        this.buggy = other.buggy;
        this.touchedBy = new ArrayList<>(other.touchedBy);
        this.authors = new HashSet<>(other.authors);
        this.metrics = new ComplexityMetrics(other.metrics);
        this.changesMetrics = new ChangesMetrics(other.changesMetrics);
    }

    public MethodKey getMethodKey() {
        return methodKey;
    }

    public void setMethodKey(MethodKey methodKey) {
        this.methodKey = methodKey;
    }

    public ComplexityMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(ComplexityMetrics metrics) {
        this.metrics = metrics;
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

    public IsBuggy getBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy.setBuggy(buggy);
    }
}
