package org.isw2.dataset.core.controller;

import org.isw2.dataset.core.controller.context.LabelingContext;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.git.model.Change;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.git.model.MyEdit;
import org.isw2.dataset.jira.model.Ticket;
import org.isw2.dataset.jira.model.Version;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class Labeling implements Controller<LabelingContext, Void> {

    @Override
    public Void execute(LabelingContext context) throws ProcessingException {
        for (Ticket ticket :  context.tickets()) {
            List<Version> affectedVersions = ticket.getAffectedVersions();
            List<Commit> fixCommits = ticket.getFixedCommits();

            if (fixCommits == null || fixCommits.isEmpty()) continue;

            fixCommits.sort(Comparator.comparing(commit -> LocalDate.parse(commit.commitTime())));

            for (Commit fixCommit : fixCommits) {
                List<Method> touchedMethods = touchedMethods(context.methodsByCommit().get(fixCommit), fixCommit);

                if (touchedMethods == null || touchedMethods.isEmpty()) continue;

                for (Method touchedMethod : touchedMethods) {
                    MethodKey key = touchedMethod.getMethodKey();
                    labelMethodInTheAffectedVersions(key, affectedVersions, context.methodsByVersion(), fixCommit);
                }
            }
        }
        return null;
    }

    private void labelMethodInTheAffectedVersions(MethodKey targetKey, List<Version> affectedVersions, Map<Version, Map<MethodKey, Method>> methodsByVersions, Commit fixCommit) {
        for (Version affectedVersion : affectedVersions) {
            LocalDate affectedVersionReleaseDate = LocalDate.parse(affectedVersion.getReleaseDate());
            LocalDate fixCommitDate = LocalDate.parse(fixCommit.commitTime());
            if (affectedVersionReleaseDate.isAfter(fixCommitDate)) {
                continue;
            }

            Map<MethodKey, Method> methodsInVersion = methodsByVersions.get(affectedVersion);
            if (methodsInVersion == null) continue;

            for (Method historicalMethod : methodsInVersion.values()) {
                if (historicalMethod.getMethodKey().equals(targetKey)) {
                    historicalMethod.setBuggy(true);
                    break;
                }
            }
        }
    }

    private List<Method> touchedMethods(Map<MethodKey, Method> touchedMethods, Commit fixCommit) {
        if (touchedMethods == null || touchedMethods.isEmpty()) return null;
        List<Method> touchedMethodList = new ArrayList<>();
        for (Method method : touchedMethods.values()) {
            if (methodIsTouchedByCommit(method, fixCommit)) touchedMethodList.add(method);
        }
        return touchedMethodList;
    }

    private boolean methodIsTouchedByCommit(Method method, Commit commit) {
        for (Change change : commit.changes()) {
            boolean sameFile = checkAddChange(method, change) || checkModifyChange(method, change);
            if (!sameFile) {
                continue;
            }
            for (MyEdit edit : change.getEdits()) {
                if (changeIsOverlappedWithMethod(edit, method)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean changeIsOverlappedWithMethod(MyEdit edit, Method method) {
        // Compute where common part start
        int methodStart = method.getStartLine() - 1; // Jgit is 0-based
        int methodEnd = method.getEndLine();
        int overlapStart = Math.max(methodStart, edit.getNewStart());
        // Compute where common part end
        int overlapEnd = Math.min(methodEnd, edit.getNewEnd());
        // There is a common part only if overlapStart < overlapEnd
        return overlapStart < overlapEnd;
    }

    private boolean checkAddChange(Method currentMethod, Change change) {
        return change.getType().equals("ADD") && change.getNewPath().equals(currentMethod.getMethodKey().path());
    }

    private boolean checkModifyChange(Method currentMethod, Change change) {
        return change.getType().equals("MODIFY") && change.getNewPath().equals(currentMethod.getMethodKey().path());
    }
}



