package org.isw2.changes.model;

public class ChangesMetrics {
    private long methodHistories = 0;
    private int authors = 0;

    public long getMethodHistories() {
        return methodHistories;
    }

    public void setMethodHistories(long methodHistories) {
        this.methodHistories = methodHistories;
    }

    public int getAuthors() {
        return authors;
    }

    public void setAuthors(int authors) {
        this.authors = authors;
    }
}
