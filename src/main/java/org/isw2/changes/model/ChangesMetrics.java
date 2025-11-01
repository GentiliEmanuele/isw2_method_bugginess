package org.isw2.changes.model;

public class ChangesMetrics {
    private long methodHistories = 0;
    private int authors = 0;
    private int stmtAdded = 0;
    private int maxStmtAdded = 0;
    private int stmtDeleted = 0;
    private int maxStmtDeleted = 0;

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

    public int getStmtAdded() {
        return stmtAdded;
    }

    public void setStmtAdded(int stmtAdded) {
        this.stmtAdded = stmtAdded;
    }

    public int getMaxStmtAdded() {
        return maxStmtAdded;
    }

    public void setMaxStmtAdded(int maxStmtAdded) {
        this.maxStmtAdded = maxStmtAdded;
    }

    public int getStmtDeleted() {
        return stmtDeleted;
    }

    public void setStmtDeleted(int stmtDeleted) {
        this.stmtDeleted = stmtDeleted;
    }

    public int getMaxStmtDeleted() {
        return maxStmtDeleted;
    }

    public void setMaxStmtDeleted(int maxStmtDeleted) {
        this.maxStmtDeleted = maxStmtDeleted;
    }
}
