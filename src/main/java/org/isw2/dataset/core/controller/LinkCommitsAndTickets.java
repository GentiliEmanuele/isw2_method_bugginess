package org.isw2.dataset.core.controller;

import org.isw2.dataset.core.controller.context.LinkCommitsAndTicketsContext;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.absfactory.Controller;
import org.isw2.dataset.git.model.Commit;
import org.isw2.dataset.jira.model.Ticket;
import org.isw2.dataset.jira.model.Version;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkCommitsAndTickets implements Controller<LinkCommitsAndTicketsContext, Void> {
    @Override
    public Void execute(LinkCommitsAndTicketsContext context) throws ProcessingException {
        // Create a map for fast access to the tickets
        Map<String, Ticket> ticketMap = new HashMap<>();
        for (Ticket ticket : context.tickets()) {
            // Extract ticket number by the key (Es. "PROJ-1103" -> "1103")
            String ticketIdNumber = ticket.getKey().split("-")[1];
            ticketMap.put(ticketIdNumber, ticket);
        }

        for (Commit commit : context.commits()) {
            String message = commit.message();

            if (message == null || message.isEmpty()) continue;

            evaluateMessage(context.projectName(), message, ticketMap, commit, context.versions());

        }
        return null;
    }

    private void evaluateMessage(String projectName, String message, Map<String, Ticket> ticketMap, Commit commit, List<Version> versions) {
        Pattern pattern = Pattern.compile("(" + projectName + "-\\d+)|(#\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            String match = matcher.group();

            // Only extract the number from the match found
            String numberStr = match.replaceAll("[^\\d]", "");

            // Look for matching with an existing ticket
            if (ticketMap.containsKey(numberStr)) {
                Ticket ticket = ticketMap.get(numberStr);

                if (ticket.getFixedCommits() == null) {
                    ticket.setFixedCommits(new ArrayList<>());
                }
                if (!ticket.getFixedCommits().contains(commit)) {
                    ticket.getFixedCommits().add(commit);
                    adjustAffectedVersions(ticket, commit, versions);
                    adjustFixedVersion(ticket, versions);
                }
            }
        }
    }

    private void adjustAffectedVersions(Ticket ticket, Commit fixedCommit, List<Version> versions) {
        LocalDate fixedCommitDate = LocalDate.parse(fixedCommit.commitTime());
        LocalDate fixedVersionReleaseDate = LocalDate.parse(ticket.getFixedVersion().getReleaseDate());
        if (fixedCommitDate.isAfter(fixedVersionReleaseDate)) {
            updateAffectedVersions(ticket, versions, fixedCommitDate);
        }
    }

    private void updateAffectedVersions(Ticket ticket, List<Version> versions, LocalDate fixDate) {
        List<Version> versionsToAdd = new ArrayList<>();
        ticket.getAffectedVersions().sort(Comparator.comparing(version -> LocalDate.parse(version.getReleaseDate())));
        LocalDate lastAffectedDate = LocalDate.parse(ticket.getAffectedVersions().getLast().getReleaseDate());
        for (Version version : versions) {
            LocalDate currentReleaseDate = LocalDate.parse(version.getReleaseDate());
            if (lastAffectedDate.isBefore(currentReleaseDate) && currentReleaseDate.isBefore(fixDate)) {
                versionsToAdd.add(version);
            }
        }
        ticket.getAffectedVersions().addAll(versionsToAdd);
    }

    private void adjustFixedVersion(Ticket ticket, List<Version> versions) {
        LocalDate lastAffectedDate = LocalDate.parse(ticket.getAffectedVersions().getLast().getReleaseDate());
        LocalDate fixDate = LocalDate.parse(ticket.getFixedCommits().getLast().commitTime());
        for (Version version : versions) {
            LocalDate currentReleaseDate = LocalDate.parse(version.getReleaseDate());
            if (lastAffectedDate.isBefore(currentReleaseDate) && currentReleaseDate.isAfter(fixDate)) {
                ticket.setFixedVersion(version);
                break;
            }
        }
    }
}
