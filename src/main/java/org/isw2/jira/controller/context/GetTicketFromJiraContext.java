package org.isw2.jira.controller.context;

import org.isw2.factory.ExecutionContext;
import org.isw2.jira.model.Version;

import java.util.List;

public record GetTicketFromJiraContext(String projectName, List<Version> allVersions) implements ExecutionContext {
}
