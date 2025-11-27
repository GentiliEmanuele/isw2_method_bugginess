package org.isw2.metrics.controller;


import org.isw2.core.model.Method;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.git.model.Change;
import org.isw2.git.model.Commit;

import java.util.List;
import java.util.Map;

public class ComputeChangesMetrics implements Controller<Map<String, List<Method>>, Void> {

    @Override
    public Void execute(Map<String, List<Method>> methodByVersionAndPath) throws ProcessingException {
        methodByVersionAndPath.forEach((key, methods) -> methods.forEach(method -> {
            if (method.getTouchedBy() != null) {
                computeMethodHistories(method);
                // computeStmtAdded(method);
                // computeStmtDeleted(method);
                computeAuthors(method);
            }
        }));
        return null;
    }

    private void computeMethodHistories(Method method) {
        int historiesCounter = 0;
        for (Commit commit : method.getTouchedBy()) {
            historiesCounter += commit.changes().size();
        }
        method.getChangesMetrics().setMethodHistories(historiesCounter);
    }

    private void computeAuthors(Method method) {
        // If the author is already registered newAuthorNr = oldAuthorNr
        for (Commit commit : method.getTouchedBy()) {
            int newAuthorsNr = method.tryToAddAuthor(commit.author());
            method.getChangesMetrics().setAuthors(newAuthorsNr);
        }
    }

    /*

    private void computeStmtAdded(Method method) {
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

    private void computeStmtDeleted(Method method) {
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

     */
}
