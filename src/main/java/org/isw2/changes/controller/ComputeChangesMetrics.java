package org.isw2.changes.controller;

import org.isw2.changes.model.Change;
import org.isw2.changes.model.Commit;
import org.isw2.complexity.model.Method;

public class ComputeChangesMetrics {

    public void computeMethodHistories(Method method) {
        int historiesCounter = 0;
        for (Commit commit : method.getTouchedBy()) {
            historiesCounter += commit.getChanges().size();
        }
        method.getChangesMetrics().setMethodHistories(historiesCounter);
    }

    public void computeAuthors(Method method, Commit currentCommit) {
        // If the author is already registered newAuthorNr = oldAuthorNr
        int newAuthorsNr = method.tryToAddAuthor(currentCommit.getAuthor());
        method.getChangesMetrics().setAuthors(newAuthorsNr);
    }

    public void computeStmtAdded(Method method) {
        int stmtAdded = 0;
        int maxStmtAdded = 0;
        int changeStmtAdded = 0;
        for (Commit commit : method.getTouchedBy()) {
            for (Change change : commit.getChanges()) {
                if (change.getType().equals("ADD")) {
                    changeStmtAdded = method.getEndLine() - method.getStartLine();
                } else if (change.getType().equals("MODIFY")) {
                    // Compute old and new lengths
                    int oldLength = change.getOldEnd() - change.getOldStart();
                    int newLength = change.getNewEnd() - change.getNewStart();
                    // Check if the method is bigger
                    changeStmtAdded = newLength - oldLength;
                }
                if (changeStmtAdded > 0) {
                    stmtAdded += changeStmtAdded;
                }
                if (changeStmtAdded > maxStmtAdded) {
                    maxStmtAdded = changeStmtAdded;
                }
            }
        }
        method.getChangesMetrics().setStmtAdded(stmtAdded);
        method.getChangesMetrics().setMaxStmtAdded(maxStmtAdded);
    }

    public void computeStmtDeleted(Method method) {
        int stmtDeleted = 0;
        int  maxStmtDeleted = 0;
        int changeStmtDeleted;
        for (Commit commit : method.getTouchedBy()) {
            for (Change change : commit.getChanges()) {
                if (change.getType().equals("MODIFY")) {
                    int oldLength = change.getOldEnd() - change.getOldStart();
                    int newLength = change.getNewEnd() - change.getNewStart();
                    changeStmtDeleted = oldLength - newLength;
                    if (changeStmtDeleted > 0) {
                        stmtDeleted += changeStmtDeleted;
                    }
                    if (changeStmtDeleted > maxStmtDeleted) {
                        maxStmtDeleted = changeStmtDeleted;
                    }
                }
            }
        }
        method.getChangesMetrics().setStmtDeleted(stmtDeleted);
        method.getChangesMetrics().setMaxStmtDeleted(maxStmtDeleted);
    }
}
