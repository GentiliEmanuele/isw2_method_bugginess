package org.isw2.dataset.jira.controller.context;

import org.isw2.dataset.jira.model.Version;

import java.util.List;

public record GetTicketFromJiraContext(String projectName, List<Version> allVersions) {
}
