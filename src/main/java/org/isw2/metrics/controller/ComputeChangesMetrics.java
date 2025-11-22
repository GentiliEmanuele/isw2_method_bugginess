package org.isw2.metrics.controller;


import org.isw2.core.model.FileClass;
import org.isw2.core.model.Method;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ExecutionContext;
import org.isw2.git.model.Change;
import org.isw2.git.model.Commit;
import org.isw2.jira.model.Version;
import org.isw2.metrics.controller.context.ComputeMetricsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComputeChangesMetrics implements Controller {

    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof ComputeMetricsContext(Map<Version, List<FileClass>> fileClassByVersion))) {
            throw new ProcessingException("Context is not a ComputeMetricsContext");
        }

        fileClassByVersion.forEach((version, fileClasses) -> {
            fileClasses.forEach(fileClass -> {
                fileClass.getMethods().forEach(method -> {
                    version.getCommits().forEach(commit -> {
                        if (methodIsToucheBy(method, commit, fileClass.getPath())) {
                            computeMethodHistories(method);
                            computeAuthors(method,  commit);
                            computeStmtAdded(method);
                            computeStmtDeleted(method);
                        }
                    });
                });
            });
        });
    }

    private boolean methodIsToucheBy(Method method, Commit commit, String classPath) {
        boolean touched = false;
        List<Change> changes = commit.getChanges();
        if (changes == null) return false;
        for (Change change : changes) {
            touched = isTouchedByAdd(change, classPath) || methodIsTouchedByModify(change, classPath, method);
            if (touched) {
                if (method.getTouchedBy() == null) {
                    method.setTouchedBy(new ArrayList<>());
                }
                method.getTouchedBy().add(commit);
            }
        }
        return touched;
    }

    private boolean isTouchedByAdd (Change change, String classPath) {
        return change.getType().equals("ADD") && change.getNewPath().equals(classPath);
    }

    private boolean methodIsTouchedByModify(Change change, String classPath, Method method) {
        return change.getType().equals("MODIFY") && change.getOldPath().equals(classPath) && change.getOldStart() == method.getStartLine() && change.getOldEnd() == method.getEndLine();
    }

    private void computeMethodHistories(Method method) {
        int historiesCounter = 0;
        for (Commit commit : method.getTouchedBy()) {
            historiesCounter += commit.getChanges().size();
        }
        method.getChangesMetrics().setMethodHistories(historiesCounter);
    }

    private void computeAuthors(Method method, Commit currentCommit) {
        // If the author is already registered newAuthorNr = oldAuthorNr
        int newAuthorsNr = method.tryToAddAuthor(currentCommit.getAuthor());
        method.getChangesMetrics().setAuthors(newAuthorsNr);
    }

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
}
