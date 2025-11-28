package org.isw2.git.controller;

import org.isw2.core.model.Method;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.git.model.Change;
import org.isw2.git.model.Commit;
import org.isw2.git.model.MyEdit;
import org.isw2.jira.model.Version;
import java.time.LocalDate;
import java.util.*;


public class GitHistoriesController implements Controller<GitHistoriesControllerContext, Void> {

    private final Map<String, LocalDate> releaseDates;

    public GitHistoriesController() {
        releaseDates = new HashMap<>();
    }

    @Override
    public Void execute(GitHistoriesControllerContext context) throws ProcessingException {

        populateReleaseDateMap(context.versions());

        Map<String, List<Commit>> historyWindowCache = new HashMap<>();

        context.methodsByVersionAndPath().forEach((key, methods) -> {
            if (methods.isEmpty()) return;

            String versionName = extractVersionFromKey(key);
            String filePath = extractPathFromKey(key);

            LocalDate releaseDate = releaseDates.get(versionName);
            if (releaseDate == null) return;

            // Get commit before (or with the same date) of release date
            List<Commit> historyWindow = historyWindowCache.computeIfAbsent(versionName, v ->
                    context.commits().stream()
                            .filter(c -> !LocalDate.parse(c.commitTime()).isAfter(releaseDate))
                            .toList()
            );

            processHistoryAndMetrics(historyWindow, filePath, methods);
        });

        return null;
    }

    private void processHistoryAndMetrics(List<Commit> historyWindow, String filePath, List<Method> methods) {
        // Map for accumulate metrics
        Map<Method, Integer> locAddedMap = new HashMap<>();
        Map<Method, Integer> locDeletedMap = new HashMap<>();
        Map<Method, Integer> maxLocAddedMap = new HashMap<>();

        for (Commit commit : historyWindow) {
            // Search the change once
            Change fileChange = findChangeForFile(commit, filePath);

            if (fileChange != null) {
                ifIsMethodTouched(methods, fileChange, commit, locAddedMap, locDeletedMap, maxLocAddedMap);
            }
        }

        // Set the metrics for the method
        for (Method method : methods) {
            if (method.getTouchedBy() != null && !method.getTouchedBy().isEmpty()) {
                method.getChangesMetrics().setMethodHistories(method.getTouchedBy().size());
                method.getChangesMetrics().setAuthors(method.getAuthors().size());

                method.getChangesMetrics().setStmtAdded(locAddedMap.getOrDefault(method, 0));
                method.getChangesMetrics().setStmtDeleted(locDeletedMap.getOrDefault(method, 0));
                method.getChangesMetrics().setMaxStmtAdded(maxLocAddedMap.getOrDefault(method, 0));
            } else {
                // Init to 0 if not touched
                method.getChangesMetrics().setMethodHistories(0);
                method.getChangesMetrics().setAuthors(0);
                method.getChangesMetrics().setStmtAdded(0);
                method.getChangesMetrics().setStmtDeleted(0);
                method.getChangesMetrics().setMaxStmtAdded(0);
            }
        }
    }

    private void ifIsMethodTouched(List<Method>  methods, Change fileChange, Commit commit, Map<Method, Integer> locAddedMap,  Map<Method, Integer> locDeletedMap, Map<Method, Integer> maxLocAddedMap) {
        for (Method method : methods) {

            // Counter for this commit
            int commitAdded = 0;
            int commitDeleted = 0;
            boolean isTouched = false;

            for (MyEdit edit : fileChange.getEdits()) {
                if (isOverlapping(method, edit)) {
                    isTouched = true;
                    commitAdded += edit.getLengthB();
                    commitDeleted += edit.getLengthA();
                }
            }

            if (isTouched) {
                // Add commit to the method history
                method.getTouchedBy().add(commit);

                // Update author
                method.tryToAddAuthor(commit.author());

                // Update global metrics
                locAddedMap.merge(method, commitAdded, Integer::sum);
                locDeletedMap.merge(method, commitDeleted, Integer::sum);

                // Update max churn
                int currentMax = maxLocAddedMap.getOrDefault(method, 0);
                if (commitAdded > currentMax) {
                    maxLocAddedMap.put(method, commitAdded);
                }
            }
        }
    }

    private Change findChangeForFile(Commit commit, String filePath) {
        if (commit.changes() == null) return null;
        for (Change change : commit.changes()) {
            if (change.getNewPath().equals(filePath)) {
                return change;
            }
        }
        return null;
    }

    private void populateReleaseDateMap(List<Version> versions) {
        if (versions != null) {
            for (Version v : versions) {
                if (v.getName() != null && v.getReleaseDate() != null) {
                    this.releaseDates.put(v.getName(), LocalDate.parse(v.getReleaseDate()));
                }
            }
        }
    }

    private String extractVersionFromKey(String key) {
        return key.split("_")[0];
    }

    private String extractPathFromKey(String key) {
        return key.split("_")[1];
    }

    private boolean isOverlapping(Method method, MyEdit edit) {
        int editStart = edit.getNewStart() + 1;
        int editEnd = edit.getNewEnd();
        return (editStart <= method.getEndLine()) && (editEnd >= method.getStartLine());
    }

}
