package org.isw2.dataset.core.controller.context;

import org.isw2.dataset.jira.model.ReturnTickets;
import org.isw2.dataset.jira.model.Version;

import java.util.List;

public record ProportionContext(List<Version> versions, ReturnTickets returnTickets) {

}
