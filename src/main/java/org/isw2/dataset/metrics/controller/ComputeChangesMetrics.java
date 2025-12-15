package org.isw2.dataset.metrics.controller;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodsKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.git.model.Author;
import org.isw2.dataset.git.model.Change;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.git.model.MyEdit;
import org.isw2.dataset.jira.model.Version;
import org.isw2.dataset.metrics.controller.context.ComputeChangesContext;

import java.util.List;
import java.util.Map;


public class ComputeChangesMetrics implements Controller<ComputeChangesContext, Void> {
    @Override
    public Void execute(ComputeChangesContext context) throws ProcessingException {
        Version prevVersion = null;
        for (Version version : context.versions()) {
            for (Commit commit : version.getCommits()) {
                for (Change change : commit.changes()) {
                    checkAndManageAdd(change, context.methodByVersionAndPath(), version, commit);
                    checkAndManageModify(change, context.methodByVersionAndPath(), version, commit);
                    checkAndManageRename(change, context.methodByVersionAndPath(), version, commit);
                    checkAndManageDeleteOrCopy(change, context.methodByVersionAndPath(), version, prevVersion, commit);
                }
            }
            prevVersion = version;
        }
        return null;
    }

    private void checkAndManageAdd(Change change, Map<MethodsKey, List<Method>> methodByVersionAndPath, Version version, Commit commit) {
        if (change.getType().equals("ADD")) {
            List<Method> methods = methodByVersionAndPath.get(new MethodsKey(version, change.getNewPath()));
            if (methods != null) {
                methods.forEach(method -> updateForAddChange(method, commit));
            }
        }
    }

    private void updateForAddChange(Method method, Commit commit) {
        updateStmtAdded(method, method.getEndLine() - method.getStartLine() + 1);
        if (!method.getTouchedBy().contains(commit)) {
            method.getTouchedBy().add(commit);
            int nrAuthors = method.tryToAddAuthor(commit.author());
            method.getChangesMetrics().setAuthors(nrAuthors);
            method.getChangesMetrics().setMethodHistories(method.getChangesMetrics().getMethodHistories() + 1);
        }
    }

    private void checkAndManageModify(Change change, Map<MethodsKey, List<Method>> methodByVersionAndPath, Version version, Commit commit) {
        if (change.getType().equals("MODIFY") && change.getOldPath().equals(change.getNewPath())) {
            List<Method> methods = methodByVersionAndPath.get(new MethodsKey(version, change.getNewPath()));
            if (methods != null) {
                methods.forEach(method -> updateForModifyChange(method, change, commit));
            }
        }
    }

    private void updateForModifyChange(Method method, Change change, Commit commit) {
        for (MyEdit edit :  change.getEdits()) {
            // If method is not touched by edit skip it
            if (!changeIsOverlappedWithMethod(edit, method)) continue;

            if (!method.getTouchedBy().contains(commit)) {
                method.getTouchedBy().add(commit);
            }

            int methodStart = method.getStartLine() - 1;
            int overlapStart = Math.max(methodStart, edit.getNewStart());
            int overlapEnd = Math.min(method.getEndLine(), edit.getNewEnd());

            // Is an add edit
            if (isAnAddEdit(edit)) {
                updateChangesMetrics(method, overlapEnd - overlapStart, 0, commit.author());
            }

            // Is a delete edit
            if (isADeleteEdit(edit)) {
                updateChangesMetrics(method, 0, overlapEnd - overlapStart, commit.author());
            }

            if (isReplaceEdit(edit)) {
                updateChangesMetrics(method, overlapEnd - overlapStart, overlapEnd - overlapStart, commit.author());
            }
        }
    }

    private void updateChangesMetrics(Method method, int stmAdded, int stmtDeleted, Author author) {
        updateStmtAdded(method, stmAdded);
        updateStmtDeleted(method, stmtDeleted);
        int nrAuthors = method.tryToAddAuthor(author);
        method.getChangesMetrics().setAuthors(nrAuthors);
        method.getChangesMetrics().setMethodHistories(method.getChangesMetrics().getMethodHistories() + 1);
    }

    private boolean isAnAddEdit(MyEdit edit) {
        return edit.getOldStart() == edit.getOldEnd() && edit.getNewStart() < edit.getNewEnd();
    }

    private boolean isADeleteEdit(MyEdit edit) {
        return edit.getOldStart() < edit.getOldEnd() && edit.getNewStart() == edit.getNewEnd();
    }

    private boolean isReplaceEdit(MyEdit edit) {
        return edit.getOldStart() < edit.getOldEnd() && edit.getNewStart() < edit.getNewEnd();
    }

    private boolean changeIsOverlappedWithMethod(MyEdit edit, Method method) {
        // Compute where common part start
        int methodStart = method.getStartLine() - 1; // Jgit is 0-based
        int overlapStart = Math.max(methodStart, edit.getNewStart());
        // Compute where common part end
        int overlapEnd = Math.min(method.getEndLine(), edit.getNewEnd());
        // There is a common part only if overlapStart < overlapEnd
        return overlapStart < overlapEnd;
    }

    private void checkAndManageRename(Change change, Map<MethodsKey, List<Method>> methodByVersionAndPath, Version version, Commit commit) {
        if (change.getType().equals("RENAME")) {
            renameInTheSameVersion(change, methodByVersionAndPath, version, commit);
        }
    }

    private void renameInTheSameVersion(Change change, Map<MethodsKey, List<Method>> methodByVersionAndPath, Version currentVersion, Commit commit) {
        MethodsKey oldKey = new MethodsKey(currentVersion, change.getOldPath());
        MethodsKey newKey = new MethodsKey(currentVersion, change.getNewPath());

        List<Method> methodsToMove = methodByVersionAndPath.get(oldKey);

        if (methodsToMove != null) {
            methodsToMove.forEach(method -> {
                method.setPath(change.getNewPath());
                if (!method.getTouchedBy().contains(commit)) {
                    method.getTouchedBy().add(commit);
                }
            });
            methodByVersionAndPath.remove(oldKey);
            methodByVersionAndPath.put(newKey, methodsToMove);
        }
    }

    private void checkAndManageDeleteOrCopy(Change change, Map<MethodsKey, List<Method>> methodByVersionAndPath, Version version, Version prevVersion, Commit commit) {
        if ((change.getType().equals("DELETE") || change.getType().equals("COPY")) && prevVersion != null) {
            List<Method> methods = methodByVersionAndPath.get(new MethodsKey(prevVersion, change.getOldPath()));
            updateMethodsForDeleteAndCopy(methods, commit);

            methods = methodByVersionAndPath.get(new MethodsKey(version, change.getOldPath()));
            updateMethodsForDeleteAndCopy(methods, commit);
        }
    }

    private void updateMethodsForDeleteAndCopy(List<Method> methods, Commit commit) {
        if (methods == null) return;
        methods.forEach(method -> {
            method.getChangesMetrics().setMethodHistories(method.getChangesMetrics().getMethodHistories() + 1);
            updateStmtDeleted(method, method.getEndLine() - method.getStartLine() + 1);
            if (!method.getTouchedBy().contains(commit)) {
                method.getTouchedBy().add(commit);
            }
            method.getChangesMetrics().setMethodHistories(method.getChangesMetrics().getMethodHistories() + 1);
            int nrAuthors = method.tryToAddAuthor(commit.author());
            method.getChangesMetrics().setAuthors(nrAuthors);
        });
    }

    private void updateStmtAdded(Method method, int stmtAdded) {
        int currentStmtAdded = method.getChangesMetrics().getStmtAdded();
        method.getChangesMetrics().setStmtAdded(currentStmtAdded + stmtAdded);
        if (stmtAdded > method.getChangesMetrics().getMaxStmtAdded()) {
            method.getChangesMetrics().setMaxStmtAdded(stmtAdded);
        }
    }

    private void updateStmtDeleted(Method method, int stmtDeleted) {
        int currentStmtDeleted = method.getChangesMetrics().getStmtDeleted();
        method.getChangesMetrics().setStmtDeleted(currentStmtDeleted + stmtDeleted);

        if (stmtDeleted > method.getChangesMetrics().getMaxStmtDeleted()) {
            method.getChangesMetrics().setMaxStmtDeleted(stmtDeleted);
        }
    }

}
