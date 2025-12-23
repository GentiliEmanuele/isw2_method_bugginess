package org.isw2.dataset.core.controller;

import org.isw2.absfactory.AbstractControllerFactory;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.core.boundary.ExitPointBoundary;
import org.isw2.dataset.core.boundary.Outcome;
import org.isw2.dataset.core.controller.context.*;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodKey;
import org.isw2.dataset.factory.*;
import org.isw2.dataset.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.jira.model.ReturnTickets;
import org.isw2.dataset.jira.model.Ticket;
import org.isw2.dataset.jira.model.Version;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;


public class EntryPointController implements Controller<EntryPointContext, Void> {

    private static final Logger logger = Logger.getLogger(EntryPointController.class.getName());
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EntryPointController.class);

    @Override
    public Void execute(EntryPointContext context) throws ProcessingException {
        List<Commit> commits;
        List<Ticket> tickets;
        List<Version> versions;

        // Get version from Jira
        logger.info("Get versions from Jira");
        AbstractControllerFactory<String, List<Version>> getVersionFromJiraFactory = new GetVersionFromJiraFactory();
        versions = getVersionFromJiraFactory.process(context.projectName());

        // Sort version list
        versions.sort(Comparator.comparing(o -> LocalDate.parse(o.getReleaseDate())));

        // Remove a percentage of versions for avoid snoring
        int total = versions.size();
        int toBeRemoved = (int)(total * context.versionDiscardPercentage());
        versions.subList(total - toBeRemoved, total).clear();

        // Get ticket from Jira
        logger.info("Get tickets from Jira");
        AbstractControllerFactory<GetTicketFromJiraContext, ReturnTickets> getTicketFromJiraFactory= new GetTicketFromJiraFactory();
        ReturnTickets returnTickets = getTicketFromJiraFactory.process(new GetTicketFromJiraContext(context.projectName(), versions));

        // Create gitController and GetCommitFromGit
        logger.info("Get commits from git");
        AbstractControllerFactory<String, List<Commit>> getCommitFactory = new GetCommitFactory();
        commits = getCommitFactory.process(context.projectName());
        commits.sort(Comparator.comparing(commit -> LocalDate.parse(commit.commitTime())));

        logger.info("Link commits and consistent tickets");
        AbstractControllerFactory<LinkCommitsAndTicketsContext, Void> linkCommitsAndTicketsFactory = new LinkCommitsAndTicketsFactory();
        linkCommitsAndTicketsFactory.process(new LinkCommitsAndTicketsContext(context.projectName(), commits, returnTickets.tickets(), versions));
        returnTickets.tickets().removeIf(ticket -> ticket.getFixedCommits() == null || ticket.getFixedCommits().size() != 1);

        // Create proportion controller and apply proportion
        logger.info("Execute proportion algorithm");
        AbstractControllerFactory<ProportionContext, Void> proportionFactory = new ProportionFactory();
        proportionFactory.process(new ProportionContext(versions, returnTickets));
        tickets = returnTickets.tickets();

        logger.info("Link commits and corrected tickets");
        linkCommitsAndTicketsFactory.process(new LinkCommitsAndTicketsContext(context.projectName(), commits, returnTickets.toBeCorrected(), versions));
        returnTickets.tickets().removeIf(ticket -> ticket.getFixedCommits() == null || ticket.getFixedCommits().size() != 1);
        returnTickets.tickets().addAll(returnTickets.toBeCorrected());

        // Merge versions and commits
        logger.info("Merge versions and commits");
        AbstractControllerFactory<MergeVersionAndCommitContext, Void> mergeVersionAndCommit = new MergeVersionAndCommitFactory();
        mergeVersionAndCommit.process(new MergeVersionAndCommitContext(versions, commits));

        // Analyze files
        logger.info("Analyze files");
        AbstractControllerFactory<AnalyzeFileContext, Map<Commit, Map<MethodKey, Method>>> analyzeFileFactory = new AnalyzeFileFactory();
        Map<Commit, Map<MethodKey, Method>> methodsByCommit = analyzeFileFactory.process(new AnalyzeFileContext(context.projectName(), versions));

        // Link methodsByCommit and versions
        logger.info("Link methodByCommit and versions");
        AbstractControllerFactory<WalkVersionsContext, Map<Version, Map<MethodKey, Method>>> walkVersionsFactory = new WalkVersionsFactory();
        Map<Version, Map<MethodKey, Method>> methodsByVersion = walkVersionsFactory.process(new WalkVersionsContext(methodsByCommit, versions));

        logger.info("Label methods");
        AbstractControllerFactory<LabelingContext, Void> labelingFactory = new LabelingFactory();
        labelingFactory.process(new LabelingContext(methodsByVersion, tickets, methodsByCommit));

        try {
            writeOutcome(context.projectName(), methodsByVersion);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private void writeOutcome(String projectName, Map<Version, Map<MethodKey, Method>> methodByVersionAndPath) throws IOException {
        List<Outcome> outcomes = new ArrayList<>();
        methodByVersionAndPath.forEach((version, methods) ->
            methods.forEach((methodKey, method) ->
                outcomes.add(createOutcome(version, method))
            )
        );
        outcomes.sort(Comparator.comparing(o -> LocalDate.parse(o.getReleaseDate())));
        ExitPointBoundary.toCsv(projectName, outcomes);

    }

    private Outcome createOutcome(Version version, Method method) {
        Outcome outcome = new Outcome();
        outcome.setClassName(method.getMethodKey().className());
        outcome.setPath(method.getMethodKey().path());
        outcome.setSignature(method.getMethodKey().signature());
        outcome.setVersion(version.getName());
        outcome.setReleaseDate(version.getReleaseDate());
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
        outcome.setBuggy(method.getBuggy().isBuggy());
        return outcome;
    }

}
