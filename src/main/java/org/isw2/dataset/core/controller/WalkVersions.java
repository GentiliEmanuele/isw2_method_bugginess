package org.isw2.dataset.core.controller;

import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.controller.context.WalkVersionsContext;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.git.model.Author;
import org.isw2.dataset.git.model.Change;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.git.model.MyEdit;
import org.isw2.dataset.jira.model.Version;
import org.isw2.dataset.metrics.model.ComplexityMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalkVersions implements Controller<WalkVersionsContext, Map<Version, Map<MethodKey, Method>>> {
    @Override
    public Map<Version, Map<MethodKey, Method>> execute(WalkVersionsContext context) throws ProcessingException {
        Map<Version, Map<MethodKey, Method>> methodsByVersion = new HashMap<>();
        // Define a registry that maintain the history between the versions
        Map<MethodKey, Method> globalRegistry = new HashMap<>();

        for (Version currentVersion : context.versions()) {
            Map<MethodKey, Method> versionAccumulator = new HashMap<>(globalRegistry);
            for (Commit currentCommit : currentVersion.getCommits()) {
                Map<MethodKey, Method> snapshot = context.methodsByCommit().get(currentCommit);
                if (snapshot == null) continue;

                manageRenames(globalRegistry, versionAccumulator, currentCommit);
                updateStateWithSnapshot(globalRegistry, versionAccumulator, snapshot, currentCommit);
            }
            methodsByVersion.put(currentVersion, versionAccumulator);
        }
        return methodsByVersion;
    }

    private void manageRenames(Map<MethodKey, Method> globalRegistry, Map<MethodKey, Method> accumulator, Commit commit) {
        if (commit.changes() == null) return;

        for (Change change : commit.changes()) {
            if ("RENAME".equals(change.getType())) {
                // Apply to rename on the two maps. True means increment the history only in the global registry.
                // False mean not increment the history.
                applyRename(globalRegistry, change, true);
                applyRename(accumulator, change, false);
            }
        }
    }

    private void updateStateWithSnapshot(Map<MethodKey, Method> globalRegistry,
                                         Map<MethodKey, Method> accumulator,
                                         Map<MethodKey, Method> snapshot,
                                         Commit currentCommit) {

        // Update existing and add new
        for (Map.Entry<MethodKey, Method> entry : snapshot.entrySet()) {
            MethodKey key = entry.getKey();
            Method snapshotMethod = entry.getValue();

            if (globalRegistry.containsKey(key)) {
                // If method exists: update static info and compute process metrics
                Method registryMethod = globalRegistry.get(key);

                // Merge static info of the snapshot
                mergeComplexityCurrentCommit(snapshotMethod, registryMethod);

                // Compute process metrics
                manageChangesCurrentCommit(registryMethod, currentCommit);

            } else {
                // If the method is new, add it
                Method newMethod = new Method(snapshotMethod);
                // Init the metrics
                newMethod.getChangesMetrics().setStmtAdded(0);
                newMethod.getChangesMetrics().setStmtDeleted(0);
                newMethod.getChangesMetrics().setMethodHistories(0);

                // Compute process metrics
                manageChangesCurrentCommit(newMethod, currentCommit);

                // Add to registry and accumulator
                globalRegistry.put(key, newMethod);
                accumulator.put(key, newMethod);
            }
        }

        // If a delete occur remove the method from global registry
        globalRegistry.keySet().removeIf(key -> !snapshot.containsKey(key));
    }


    private void applyRename(Map<MethodKey, Method> registry, Change change, boolean increment) {
        if (change.getType().equals("RENAME")) {

            // Search in the registry all method with the oldPath in the key
            List<MethodKey> keysToMove = new ArrayList<>();
            for (MethodKey key : registry.keySet()) {
                if (key.path().equals(change.getOldPath())) {
                    keysToMove.add(key);
                }
            }

            // Do the swap
            for (MethodKey oldKey : keysToMove) {
                // Remove the method from the old position
                Method method = registry.remove(oldKey);

                if (method != null) {
                    // Create the key with the new path (className and signature not change)
                    MethodKey newKey = new MethodKey(change.getNewPath(), oldKey.className(), oldKey.signature());

                    // Update the key of the method
                    method.setMethodKey(newKey);

                    if (increment)
                        method.getChangesMetrics().setMethodHistories(method.getChangesMetrics().getMethodHistories() + 1);

                    // Reinsert the method with the new key
                    registry.put(newKey, method);
                }
            }
        }
    }

    private void checkAndManageDelete(Map<MethodKey, Method> registry, Change change) {
        if (change.getType().equals("DELETE")) {
            String oldPath = change.getOldPath();
            // Remove all method associated with the deleted file
            registry.keySet().removeIf(key -> key.path().equals(oldPath));
        }
    }

    private void mergeMethodInformation(Method currentMethod, Method pastMethod, Commit currentCommit) {
        manageChangesCurrentCommit(currentMethod, currentCommit);
        mergeComplexityCurrentCommit(currentMethod, pastMethod);
        mergeChangesMetrics(currentMethod, pastMethod);
    }

    private void mergeComplexityCurrentCommit(Method currentMethod, Method pastMethod) {
        pastMethod.setMetrics(new ComplexityMetrics(currentMethod.getMetrics()));
    }

    private void mergeChangesMetrics(Method currentMethod, Method pastMethod) {
        long currentMethodHistories = currentMethod.getChangesMetrics().getMethodHistories();
        mergeMethodHistories(pastMethod, currentMethodHistories);

        int currentStmtAdded = currentMethod.getChangesMetrics().getStmtAdded();
        int currentMaxStmtAdded = currentMethod.getChangesMetrics().getMaxStmtAdded();
        mergeStmtAdded(pastMethod, currentStmtAdded, currentMaxStmtAdded);

        int currentStmtDeleted = currentMethod.getChangesMetrics().getStmtDeleted();
        int currentMaxStmtDeleted = currentMethod.getChangesMetrics().getMaxStmtDeleted();
        mergeStmtDeleted(pastMethod, currentStmtDeleted, currentMaxStmtDeleted);

        pastMethod.getTouchedBy().addAll(currentMethod.getTouchedBy());

        pastMethod.getAuthors().addAll(currentMethod.getAuthors());
    }

    private void mergeMethodHistories(Method method, long newMethodHistories) {
        method.getChangesMetrics().setMethodHistories(method.getChangesMetrics().getMethodHistories() + newMethodHistories);
    }

    private void mergeStmtAdded(Method method, int newStmtAdded, int newMaxStmtAdded) {
        method.getChangesMetrics().setStmtAdded(method.getChangesMetrics().getStmtAdded() + newStmtAdded);
        if (method.getChangesMetrics().getMaxStmtAdded() < newMaxStmtAdded) {
            method.getChangesMetrics().setMaxStmtAdded(newMaxStmtAdded);
        }
    }

    private void mergeStmtDeleted(Method method, int newStmtDeleted, int newMaxStmtDeleted) {
        method.getChangesMetrics().setStmtDeleted(method.getChangesMetrics().getStmtDeleted() + newStmtDeleted);
        if (method.getChangesMetrics().getMaxStmtDeleted() < newMaxStmtDeleted) {
            method.getChangesMetrics().setMaxStmtDeleted(newMaxStmtDeleted);
        }
    }

    private void manageChangesCurrentCommit(Method currentMethod, Commit currentCommit) {
        for (Change change : currentCommit.changes()) {

            boolean touched = checkAndManageAddChange(currentMethod, change, currentCommit.author()) ||
                    checkAndManageModifyChange(currentMethod, change, currentCommit.author());

            if (touched) {
                currentMethod.getTouchedBy().add(currentCommit);
            }
        }
    }

    private boolean checkAndManageAddChange(Method currentMethod, Change change, Author author) {
        if (change.getType().equals("ADD") && change.getNewPath().equals(currentMethod.getMethodKey().path())) {
            updateChangesMetrics(currentMethod, currentMethod.getEndLine() - currentMethod.getStartLine() + 1, 0, author);
            return true;
        }
        return false;
    }

    private boolean checkAndManageModifyChange(Method currentMethod, Change change, Author author) {
        if (change.getType().equals("MODIFY") && change.getNewPath().equals(currentMethod.getMethodKey().path())) {
            return updateForModifyChange(currentMethod, change, author);
        }
        return false;
    }

    private boolean updateForModifyChange(Method method, Change change, Author author) {
        boolean isTouched = false;
        for (MyEdit edit :  change.getEdits()) {
            // If method is not touched by edit skip it
            if (!changeIsOverlappedWithMethod(edit, method)) continue;

            isTouched = true;

            int methodStart = method.getStartLine() - 1;
            int overlapStart = Math.max(methodStart, edit.getNewStart());
            int overlapEnd = Math.min(method.getEndLine(), edit.getNewEnd());

            // Is an add edit
            if (isAnAddEdit(edit)) {
                updateChangesMetrics(method, overlapEnd - overlapStart, 0, author);
            }

            // Is a delete edit
            if (isADeleteEdit(edit)) {
                updateChangesMetrics(method, 0, overlapEnd - overlapStart, author);
            }

            // Is a replacement edit
            if (isReplaceEdit(edit)) {
                updateChangesMetrics(method, overlapEnd - overlapStart, overlapEnd - overlapStart, author);
            }
        }
        return isTouched;
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

    private void updateChangesMetrics(Method method, int stmtAdded, int stmtDeleted, Author author) {
        updateStmtAdded(method, stmtAdded);
        updateStmtDeleted(method, stmtDeleted);
        incrementMethodHistories(method);
        updateAuthorsList(method, author);
    }

    private void updateStmtAdded(Method method, int stmtAdded) {
        method.getChangesMetrics().setStmtAdded(method.getChangesMetrics().getStmtAdded() + stmtAdded);

        if (stmtAdded > method.getChangesMetrics().getMaxStmtAdded()) {
            method.getChangesMetrics().setMaxStmtAdded(stmtAdded);
        }
    }

    private void updateStmtDeleted(Method method, int stmtDeleted) {
        method.getChangesMetrics().setStmtDeleted(method.getChangesMetrics().getStmtDeleted() + stmtDeleted);

        if (stmtDeleted > method.getChangesMetrics().getMaxStmtDeleted()) {
            method.getChangesMetrics().setMaxStmtDeleted(stmtDeleted);
        }
    }

    private void incrementMethodHistories(Method method) {
        method.getChangesMetrics().setMethodHistories(method.getChangesMetrics().getMethodHistories() + 1);
    }

    private void updateAuthorsList(Method method, Author author) {
        int nrAuthors = method.tryToAddAuthor(author);
        method.getChangesMetrics().setAuthors(nrAuthors);
    }

    private boolean changeIsOverlappedWithMethod(MyEdit edit, Method method) {
        // Compute where common part start
        int methodStart = method.getStartLine() - 1; // Jgit is 0-based
        int methodEnd = method.getEndLine() - 1;
        int overlapStart = Math.max(methodStart, edit.getNewStart());
        // Compute where common part end
        int overlapEnd = Math.min(methodEnd, edit.getNewEnd());
        // There is a common part only if overlapStart < overlapEnd
        return overlapStart < overlapEnd;
    }

}
