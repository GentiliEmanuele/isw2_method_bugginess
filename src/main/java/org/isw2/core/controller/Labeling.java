package org.isw2.core.controller;

import org.isw2.core.controller.context.LabelingContext;
import org.isw2.core.model.Method;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.git.model.Change;
import org.isw2.jira.model.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Labeling implements Controller<LabelingContext, Void> {

    @Override
    public Void execute(LabelingContext context) throws ProcessingException {
        context.tickets().forEach(ticket ->
            ticket.getFixedVersion().getCommits().forEach(commit ->
                commit.changes().forEach(change ->
                    setMethodAsBuggy(ticket.getFixedVersion(), change, context.methodsByVersionAndPath())
                )
            )
        );
        return null;
    }

    private void setMethodAsBuggy(Version version, Change change, Map<String, List<Method>> methodsByVersionAndPath) {
        if (change.getType().equals("MODIFY")) {
            List<Method> methods = methodsByVersionAndPath.getOrDefault(version.getName() + "_" + change.getNewPath(), new ArrayList<>());
            methods.forEach(method ->
                change.getEdits().forEach(edit -> {
                    if (edit.getOldStart() <= method.getEndLine() && edit.getOldEnd() >= method.getStartLine())
                        method.setBuggy(1);
                })
            );
        }
    }

}
