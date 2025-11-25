package org.isw2.core.controller.context;

import org.isw2.core.model.Method;
import org.isw2.jira.model.Ticket;

import java.util.List;
import java.util.Map;

public record LabelingContext(Map<String, List<Method>> methodsByVersionAndPath, List<Ticket> tickets) {
}
