package org.isw2.changes.model;

import java.util.List;

public class Commit {
    private String id;
    private Author author;
    private String commitTime;
    private List<Change> changes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(String commitTime) {
        this.commitTime = commitTime;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "id='" + id + '\'' +
                ", author=" + author +
                ", commitTime='" + commitTime + '\'' +
                ", changes=" + changes +
                '}';
    }
}
