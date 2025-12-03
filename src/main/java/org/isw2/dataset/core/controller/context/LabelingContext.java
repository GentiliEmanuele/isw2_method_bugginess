package org.isw2.dataset.core.controller.context;

import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodsKey;
import org.isw2.dataset.jira.model.Ticket;

import java.util.List;
import java.util.Map;

public record LabelingContext(Map<MethodsKey, List<Method>> methodsByVersionAndPath, List<Ticket> tickets) {
}
