package org.isw2.core.controller;

import org.isw2.core.boundary.ExitPointBoundary;
import org.isw2.core.boundary.Outcome;
import org.isw2.core.controller.context.EntryPointContext;
import org.isw2.core.model.Method;
import org.isw2.git.controller.context.GetCommitFromGitContext;
import org.isw2.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.core.controller.context.MapCommitsAndMethodContext;
import org.isw2.core.controller.context.MergeVersionAndCommitContext;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ControllerFactory;
import org.isw2.factory.ControllerType;
import org.isw2.factory.ExecutionContext;
import org.isw2.git.controller.GetCommitFromGit;
import org.isw2.git.controller.GitController;
import org.isw2.git.model.Commit;
import org.isw2.jira.controller.GetTicketFromJira;
import org.isw2.jira.controller.GetVersionsFromJira;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class EntryPointController implements Controller {

    private List<Version> versions;
    private List<Ticket> tickets;
    private List<Commit> commits;

    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof EntryPointContext(String projectName))) {
            throw new IllegalArgumentException("Required params: EntryPointContext. Received: " +
                    (context != null ? context.getClass().getSimpleName() : "null"));
        }

        // Get version from Jira
        getVersionsFromJira(context);

        // Get ticket from Jira
        getTicketsFromJira(new GetTicketFromJiraContext(projectName, versions));

        // Create gitController and GetCommitFromGit
        Controller gitController = ControllerFactory.createController(ControllerType.GIT_CONTROLLER);
        if (gitController instanceof GitController controller) {

            // Get commits from git
            getCommitsFromGit(new GetCommitFromGitContext(projectName, controller));

            // Merge versions and commits
            mergeVersionsAndCommits();

            // Map commit, method and tickets
            mapCommitsMethods(new MapCommitsAndMethodContext(projectName, versions, controller, tickets));

        } else {
            throw new ProcessingException("Controller is not a GitController");
        }

    }

    private void getVersionsFromJira(ExecutionContext context) throws ProcessingException {
        Controller getVersionsFromJira = ControllerFactory.createController(ControllerType.GET_VERSION_FROM_JIRA);
        getVersionsFromJira.execute(context);
        if (getVersionsFromJira instanceof GetVersionsFromJira controller) {
            versions = new ArrayList<>(controller.getJiraVersions());
        } else {
            throw new ProcessingException("Processing error occurred");
        }
    }

    private void getTicketsFromJira(ExecutionContext context) throws ProcessingException {
        Controller getTicketFromJira = ControllerFactory.createController(ControllerType.GET_TICKET_FROM_JIRA);
        getTicketFromJira.execute(context);
        if (getTicketFromJira instanceof GetTicketFromJira controller) {
            tickets = new ArrayList<>(controller.getJiraTickets());
        } else {
            throw new ProcessingException("Processing error occurred");
        }
    }

    private void getCommitsFromGit(ExecutionContext context) throws ProcessingException {
        Controller getCommitFromGit = ControllerFactory.createController(ControllerType.GET_COMMIT_FROM_GIT);
        getCommitFromGit.execute(context);
        if (getCommitFromGit instanceof GetCommitFromGit controller) {
            commits = new ArrayList<>(controller.getCommits());
        } else {
            throw new ProcessingException("Processing error occurred");
        }
    }

    private void mergeVersionsAndCommits() throws ProcessingException {
        Controller mergeVersionAndCommit = ControllerFactory.createController(ControllerType.MERGE_VERSION_AND_COMMIT);
        mergeVersionAndCommit.execute(new MergeVersionAndCommitContext(versions, commits));
    }

    private void mapCommitsMethods(ExecutionContext context) throws ProcessingException {
        if (context instanceof MapCommitsAndMethodContext(String projectName, _, _, _)) {
            Controller mapCommitsMethods = ControllerFactory.createController(ControllerType.MAP_COMMITS_AND_METHODS);
            mapCommitsMethods.execute(context);
            if (mapCommitsMethods instanceof MapCommitsAndMethods controller) {
                try {
                    writeOutcome(projectName, controller.getMethodsByVersion());
                } catch (IOException _) {
                    throw new ProcessingException("An error occurred while writing the results");
                }
            } else {
                throw new ProcessingException("Processing error occurred");
            }
        } else  {
            throw new ProcessingException("Controller is not a MapCommitsAndMethodContext");
        }
    }

    private void writeOutcome(String projectName, Map<Version, List<Method>> methodsByVersion) throws IOException {
        List<Outcome> outcomes = new ArrayList<>();
        methodsByVersion.forEach((version, methods) -> methods.forEach(method -> outcomes.add(createOutcome(version.getName(), method))));
        ExitPointBoundary.toCsv(projectName, outcomes);

    }

    private Outcome createOutcome(String version, Method method) {
        Outcome outcome = new Outcome();
        outcome.setClassName(method.getClassName());
        outcome.setSignature(method.getSignature());
        outcome.setVersion(version);
        outcome.setLinesOfCode(method.getMetrics().getLinesOfCode());
        outcome.setStatementsCount(method.getMetrics().getStatementsCount());
        outcome.setCyclomaticComplexity(method.getMetrics().getCyclomaticComplexity());
        outcome.setCognitiveComplexity(method.getMetrics().getCognitiveComplexity());
        outcome.setVocabulary(method.getMetrics().getHalsteadComplexity().getVocabulary());
        outcome.setProgramLength(method.getMetrics().getHalsteadComplexity().getProgramLength());
        outcome.setEstimatedProgramLength(method.getMetrics().getHalsteadComplexity().getProgramLength());
        outcome.setVolume(method.getMetrics().getHalsteadComplexity().getVolume());
        outcome.setDifficulty(method.getMetrics().getHalsteadComplexity().getDifficulty());
        outcome.setEffort(method.getMetrics().getHalsteadComplexity().getEffort());
        outcome.setNestingDepth(method.getMetrics().getNestingDepth());
        outcome.setNumberOfBranchesAndDecisionPoint(method.getMetrics().getNumberOfBranchesAndDecisionPoint());
        outcome.setParameterCount(method.getMetrics().getParameterCount());
        outcome.setCodeSmellCounter(method.getMetrics().getCodeSmellCounter());
        outcome.setMethodHistories(method.getChangesMetrics().getMethodHistories());
        outcome.setAuthors(method.getChangesMetrics().getAuthors());
        outcome.setStmtAdded(method.getChangesMetrics().getStmtAdded());
        outcome.setMaxStmtAdded(method.getChangesMetrics().getMaxStmtAdded());
        outcome.setStmtDeleted(method.getChangesMetrics().getStmtDeleted());
        outcome.setMaxStmtDeleted(method.getChangesMetrics().getMaxStmtDeleted());
        outcome.setStartLine(method.getStartLine());
        outcome.setEndLine(method.getEndLine());
        return outcome;
    }

}
