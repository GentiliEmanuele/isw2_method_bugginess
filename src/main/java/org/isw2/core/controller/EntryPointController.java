package org.isw2.core.controller;

import org.isw2.core.boundary.ExitPointBoundary;
import org.isw2.core.boundary.Outcome;
import org.isw2.core.controller.context.*;
import org.isw2.core.model.Method;
import org.isw2.factory.*;
import org.isw2.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.exceptions.ProcessingException;
import org.isw2.git.model.Commit;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;

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
        AbstractControllerFactory<GetTicketFromJiraContext, List<Ticket>> getTicketFromJiraFactory= new GetTicketFromJiraFactory();
        tickets = getTicketFromJiraFactory.process(new GetTicketFromJiraContext(projectName, versions));

        // Create proportion controller and apply proportion
        logger.info("Execute proportion algorithm");
        AbstractControllerFactory<ProportionContext, Void> proportionFactory = new ProportionFactory();
        proportionFactory.process(new ProportionContext(versions, tickets));

        // Create gitController and GetCommitFromGit
        logger.info("Get commits from git");
        AbstractControllerFactory<String, List<Commit>> getCommitFactory = new GetCommitFactory();
        commits = getCommitFactory.process(projectName);

        logger.info("Link commits and tickets");
        AbstractControllerFactory<LinkCommitsAndTicketsContext, Void> linkCommitsAndTicketsFactory = new LinkCommitsAndTicketsFactory();
        linkCommitsAndTicketsFactory.process(new LinkCommitsAndTicketsContext(projectName, commits, tickets));

        // Merge versions and commits
        logger.info("Merge versions and commits");
        AbstractControllerFactory<MergeVersionAndCommitContext, Void> mergeVersionAndCommit = new MergeVersionAndCommitFactory();
        mergeVersionAndCommit.process(new MergeVersionAndCommitContext(versions, commits));

        // Analyze files
        logger.info("Analyze files");
        AbstractControllerFactory<AnalyzeFileContext, Map<String, List<Method>>> analyzeFileFactory = new AnalyzeFileFactory();
        Map<String, List<Method>> methodByVersionAndPath = analyzeFileFactory.process(new AnalyzeFileContext(projectName, versions));

        // Compute changes metrics
        AbstractControllerFactory<Map<String, List<Method>>, Void> computeChangesMetricsFactory = new ComputeChangesMetricsFactory();
        computeChangesMetricsFactory.process(methodByVersionAndPath);

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

    private void writeOutcome(String projectName, Map<String, List<Method>> methodByVersionAndPath) throws IOException {
        List<Outcome> outcomes = new ArrayList<>();
        methodByVersionAndPath.forEach((key, methods) ->
            methods.forEach(method ->
                outcomes.add(createOutcome(key.split("_")[0], method))
            )
        );
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
        outcome.setBuggy(method.getBuggy());
        return outcome;
    }

}
