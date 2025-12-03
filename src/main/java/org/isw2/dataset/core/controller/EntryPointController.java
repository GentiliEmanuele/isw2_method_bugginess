package org.isw2.dataset.core.controller;

import org.isw2.dataset.core.boundary.ExitPointBoundary;
import org.isw2.dataset.core.boundary.Outcome;
import org.isw2.dataset.core.controller.context.*;
import org.isw2.dataset.core.model.Method;
import org.isw2.dataset.core.model.MethodsKey;
import org.isw2.dataset.factory.*;
import org.isw2.dataset.git.controller.GitHistoriesControllerContext;
import org.isw2.dataset.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.jira.model.ReturnTickets;
import org.isw2.dataset.jira.model.Ticket;
import org.isw2.dataset.jira.model.Version;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;


public class EntryPointController implements Controller<String, Void> {

    private static final Logger logger = Logger.getLogger(EntryPointController.class.getName());

    @Override
    public Void execute(String projectName) throws ProcessingException {
        List<Commit> commits;
        List<Ticket> tickets;
        List<Version> versions;

        // Get version from Jira
        logger.info("Get versions from Jira");
        AbstractControllerFactory<String, List<Version>> getVersionFromJiraFactory = new GetVersionFromJiraFactory();
        versions = getVersionFromJiraFactory.process(projectName);

        // Sort version list
        versions.sort(Comparator.comparing(o -> LocalDate.parse(o.getReleaseDate())));

        // Get ticket from Jira
        logger.info("Get tickets from Jira");
        AbstractControllerFactory<GetTicketFromJiraContext, ReturnTickets> getTicketFromJiraFactory= new GetTicketFromJiraFactory();
        ReturnTickets returnTickets = getTicketFromJiraFactory.process(new GetTicketFromJiraContext(projectName, versions));

        // Create proportion controller and apply proportion
        logger.info("Execute proportion algorithm");
        AbstractControllerFactory<ProportionContext, Void> proportionFactory = new ProportionFactory();
        proportionFactory.process(new ProportionContext(versions, returnTickets));
        tickets = returnTickets.tickets();

        // Create gitController and GetCommitFromGit
        logger.info("Get commits from git");
        AbstractControllerFactory<String, List<Commit>> getCommitFactory = new GetCommitFactory();
        commits = getCommitFactory.process(projectName);
        commits.sort(Comparator.comparing(commit -> LocalDate.parse(commit.commitTime())));

        logger.info("Link commits and tickets");
        AbstractControllerFactory<LinkCommitsAndTicketsContext, Void> linkCommitsAndTicketsFactory = new LinkCommitsAndTicketsFactory();
        linkCommitsAndTicketsFactory.process(new LinkCommitsAndTicketsContext(projectName, commits, tickets));

        // Merge versions and commits
        logger.info("Merge versions and commits");
        AbstractControllerFactory<MergeVersionAndCommitContext, Void> mergeVersionAndCommit = new MergeVersionAndCommitFactory();
        mergeVersionAndCommit.process(new MergeVersionAndCommitContext(versions, commits));

        // Analyze files
        logger.info("Analyze files");
        AbstractControllerFactory<AnalyzeFileContext, Map<MethodsKey, List<Method>>> analyzeFileFactory = new AnalyzeFileFactory();
        Map<MethodsKey, List<Method>> methodByVersionAndPath = analyzeFileFactory.process(new AnalyzeFileContext(projectName, versions));

        // Compute GitHistories
        logger.info("Compute git histories");
        AbstractControllerFactory<GitHistoriesControllerContext, Void> gitHistoriesControllerFactory = new GitHistoriesControllerFactory();
        gitHistoriesControllerFactory.process(new GitHistoriesControllerContext(methodByVersionAndPath, commits, versions));

        // Methods labeling
        AbstractControllerFactory<LabelingContext, Void> labelingController = new LabelingFactory();
        labelingController.process(new LabelingContext(methodByVersionAndPath, tickets));

        // Write result on CSV
        try {
            writeOutcome(projectName, methodByVersionAndPath);
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }

        return null;
    }

    private void writeOutcome(String projectName, Map<MethodsKey, List<Method>> methodByVersionAndPath) throws IOException {
        List<Outcome> outcomes = new ArrayList<>();
        methodByVersionAndPath.forEach((key, methods) ->
            methods.forEach(method ->
                outcomes.add(createOutcome(key.version(), method))
            )
        );
        outcomes.sort(Comparator.comparing(o -> LocalDate.parse(o.getReleaseDate())));
        ExitPointBoundary.toCsv(projectName, outcomes);

    }

    private Outcome createOutcome(Version version, Method method) {
        Outcome outcome = new Outcome();
        outcome.setClassName(method.getClassName());
        outcome.setPath(method.getPath());
        outcome.setSignature(method.getSignature());
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
        outcome.setBuggy(method.getBuggy());
        return outcome;
    }

}
