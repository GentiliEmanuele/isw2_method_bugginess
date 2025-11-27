package org.isw2.core.controller;

import org.isw2.core.controller.context.LinkCommitsAndTicketsContext;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.git.model.Commit;
import org.isw2.jira.model.Ticket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

            evaluateMessage(context.projectName(), message, ticketMap, commit);

        }
        return null;
    }

    /**
     * Search, in the message, strings that match with the regex
     * @param projectName: name of the project
     * @param message: commit message
     * @param ticketMap: map for fast search of the tickets
     */
    private void  evaluateMessage(String projectName, String message, Map<String, Ticket> ticketMap, Commit commit) {
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
                }
            }
        }

    }
}
