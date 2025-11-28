package org.isw2.core.controller;

import org.isw2.core.controller.context.LabelingContext;
import org.isw2.core.model.Method;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.git.model.Change;
import org.isw2.git.model.Commit;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;

import java.util.List;
import java.util.Map;


public class Labeling implements Controller<LabelingContext, Void> {

    @Override
    public Void execute(LabelingContext context) throws ProcessingException {
        // Iterate on buggy ticket
        for (Ticket ticket : context.tickets()) {

            // Get fixed commits
            List<Commit> fixCommits = ticket.getFixedCommits();

            // Get affected version
            List<Version> affectedVersions = ticket.getAffectedVersions();

            // Continue only if both information are available
            if (fixCommits != null && !fixCommits.isEmpty() && affectedVersions != null && !affectedVersions.isEmpty()) {
                iterateFixedCommits(fixCommits, affectedVersions, context.methodsByVersionAndPath());
            }
        }
        return null;
    }

    private void iterateFixedCommits(List<Commit> fixCommits, List<Version> affectedVersions, Map<String, List<Method>> methodsByVersionAndPath) {
        // For all fixed commit: problem is solved
        for (Commit fixCommit : fixCommits) {
            // What code was changed to resolve the error?
            for (Change change : fixCommit.changes()) {
                // Since when was this incorrect code there?
                for (Version affectedVersion : affectedVersions) {
                    labelBuggyMethodsInVersion(affectedVersion, change, methodsByVersionAndPath);
                }
            }
        }
    }

    private void labelBuggyMethodsInVersion(Version version, Change fixChange, Map<String, List<Method>> methodsByVersionAndPath) {
        // A bug is fixed by MODIFY or DELETE changes
        if (fixChange.getType().equals("MODIFY") || fixChange.getType().equals("DELETE")) {

            // Get methods in the touched file
            String key = version.getName() + "_" + fixChange.getOldPath();
            List<Method> methodsInAffectedVersion = methodsByVersionAndPath.get(key);

            if (methodsInAffectedVersion != null) {
                for (Method method : methodsInAffectedVersion) {
                    // Check if the method intersects with the change
                    if (isMethodTouchedByFix(method, fixChange)) {
                        method.setBuggy(1);
                    }
                }
            }
        }
    }

    private boolean isMethodTouchedByFix(Method method, Change fixChange) {
        // Iterate on each edit
        for (var edit : fixChange.getEdits()) {

            int editStart = edit.getOldStart();
            int editEnd = edit.getOldEnd();

            if (editStart <= method.getEndLine() && editEnd >= method.getStartLine()) {
                return true;
            }
        }
        return false;
    }
}



