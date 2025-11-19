package org.isw2.core.controller.context;

import org.isw2.core.model.Method;
import org.isw2.factory.ExecutionContext;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;

import java.util.List;
import java.util.Map;

public record LabelingContext(Map<Version, List<Method>> methodByVersion, List<Ticket> tickets) implements ExecutionContext {
}
