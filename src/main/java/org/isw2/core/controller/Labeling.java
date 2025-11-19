package org.isw2.core.controller;

import org.isw2.core.controller.context.LabelingContext;
import org.isw2.core.model.Method;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ExecutionContext;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;

import java.util.List;
import java.util.Map;

public class Labeling implements Controller {
    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof LabelingContext(Map<Version, List<Method>> methodByVersion, List<Ticket> tickets))) {
            throw new IllegalArgumentException("Required params: LabelingContext. Received: " +
                    (context != null ? context.getClass().getSimpleName() : "null"));
        }
        methodByVersion.forEach((version, methods) -> {
           methods.forEach(method -> {
               if (method.getTouchedBy() != null) {
                   method.getTouchedBy().forEach(commit -> {
                       if (version.getCommits().contains(commit) && itAffectedVersion(tickets, version)) {
                           method.setBuggy(1);
                       }
                   });
               }
           });
        });
    }

    private boolean itAffectedVersion(List<Ticket> tickets, Version version) {
        for (Ticket ticket : tickets) {
            if (ticket.getAffectedVersions().contains(version)) {
                return true;
            }
        }
        return false;
    }
}
