package org.isw2.jira.controller;

import org.isw2.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.exceptions.ProcessingException;
import org.isw2.factory.Controller;
import org.isw2.factory.ExecutionContext;
import org.isw2.jira.model.Ticket;
import org.isw2.jira.model.Version;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.isw2.jira.controller.GetVersionsFromJira.readJsonFromUrl;

public class GetTicketFromJira implements Controller {

    private static final String FIX_VERSIONS = "fixVersions";
    private static final String FIELDS = "fields";
    private static final String RELEASE_DATE = "releaseDate";
    private static final String NAME = "name";
    private static final String TOTAL = "total";
    private static final String AFFECTED_VERSIONS = "versions";
    private static final String ISSUES = "issues";
    private static final String KEY = "key";
    private static final String DATE = "created";

    private final List<Ticket> tickets = new ArrayList<>();

    @Override
    public void execute(ExecutionContext context) throws ProcessingException {
        if (!(context instanceof GetTicketFromJiraContext(String projectName, List<Version> allVersions))) {
            throw new IllegalArgumentException("Required params: GetTicketFromJiraContext. Received: " +
                    (context != null ? context.getClass().getSimpleName() : "null"));
        }

        try {
            getTicketFromJira(projectName, allVersions);
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    public List<Ticket> getJiraTickets() {
        return tickets;
    }

    private void getTicketFromJira(String projectName, List<Version> allVersions) throws IOException {
        int i =  0;
        int j;
        int total;
        String url;
        do {
            j = i + 1000;
            url = formatUrl(projectName, i, j);
            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray(ISSUES);
            total = json.getInt(TOTAL);
            // Iterate through each bug
            for (; i < total && i < j; i++) {
                // Extract fixedVersions from json
                JSONArray jsonFixedVersions = issues.getJSONObject(i % 1000).getJSONObject(FIELDS).getJSONArray(FIX_VERSIONS);
                List<Version> fixedVersions = extractVersions(jsonFixedVersions, allVersions);

                // Extract affectedVersions from json
                JSONArray jsonAffectedVersions = issues.getJSONObject(i % 1000).getJSONObject(FIELDS).getJSONArray(AFFECTED_VERSIONS);
                List<Version> affectedVersions = extractVersions(jsonAffectedVersions, allVersions);

                // Get key and date from json
                String key = issues.getJSONObject(i % 1000).getString(KEY);
                String date = issues.getJSONObject(i % 1000).getJSONObject(FIELDS).getString(DATE);

                // Consider only ticket with FV and AV
                if (!fixedVersions.isEmpty() && !affectedVersions.isEmpty()) {
                    Ticket ticket = new Ticket(key, affectedVersions, affectedVersions.getLast(), affectedVersions.getFirst(), getOpeningVersionFromDate(date, allVersions));
                    tickets.add(ticket);
                }
            }
        } while (i < total);
        tickets.sort(Comparator.comparing(ticket -> ticket.getFixedVersion().getReleaseDate()));
    }

    private List<Version> extractVersions(JSONArray jsonVersions, List<Version> allVersions) {
        List<Version> versions = new ArrayList<>();
        for (int i = 0; i < jsonVersions.length(); i++) {
            JSONObject current  = jsonVersions.getJSONObject(i);
            // Consider only the released fixVersions
            if (!current.has(RELEASE_DATE) || !current.has(NAME)) continue;
            for (Version version : allVersions) {
                if (version.getReleaseDate().equals(current.getString(RELEASE_DATE)) && version.getName().equals(current.getString("name"))) {
                    versions.add(version);
                }
            }
        }
        return versions;
    }

    private Version getOpeningVersionFromDate(String date, List<Version> allVersions) {
        Version openingVersion = allVersions.getFirst();
        LocalDate openingLocalDate = string2LocalDate(date);
        for (int i = 0; i < allVersions.size() - 1; i++) {
            // Get current and next version date
            LocalDate currentReleaseDate = LocalDate.parse(allVersions.get(i).getReleaseDate());
            LocalDate nextReleaseDate = LocalDate.parse(allVersions.get(i + 1).getReleaseDate());

            // If the opening date is after the first version date make null the opening version
            if (openingVersion != null && openingLocalDate.isAfter(LocalDate.parse(openingVersion.getReleaseDate()))) {
                openingVersion = null;
            // else if the opening date is between two version set as opening version the more recently version
            } else if (openingLocalDate.isAfter(currentReleaseDate) && openingLocalDate.isBefore(nextReleaseDate) || openingLocalDate.isEqual(nextReleaseDate)) {
                openingVersion = allVersions.get(i + 1);
                break;
            }
        }
        return openingVersion;
    }

    private String formatUrl(String projectName, Integer i, Integer j) {
        return "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,fixVersions,created&startAt="
                + i.toString() + "&maxResults=" + j.toString();
    }

    private LocalDate string2LocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        OffsetDateTime odt = OffsetDateTime.parse(date, formatter);
        return odt.toLocalDate();
    }

}
