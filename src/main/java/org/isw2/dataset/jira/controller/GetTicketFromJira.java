package org.isw2.dataset.jira.controller;

import org.isw2.dataset.jira.controller.context.GetTicketFromJiraContext;
import org.isw2.dataset.exceptions.ProcessingException;
import org.isw2.dataset.factory.Controller;
import org.isw2.dataset.jira.model.ReturnTickets;
import org.isw2.dataset.jira.model.Ticket;
import org.isw2.dataset.jira.model.Version;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.isw2.dataset.jira.controller.GetVersionsFromJira.readJsonFromUrl;

public class GetTicketFromJira implements Controller<GetTicketFromJiraContext, ReturnTickets> {

    private static final String FIELDS = "fields";
    private static final String RELEASE_DATE = "releaseDate";
    private static final String NAME = "name";
    private static final String TOTAL = "total";
    private static final String AFFECTED_VERSIONS = "versions";
    private static final String ISSUES = "issues";
    private static final String KEY = "key";
    private static final String CREATED = "created";
    private static final String RESOLUTION_DATE = "resolutiondate";

    @Override
    public ReturnTickets execute(GetTicketFromJiraContext context) throws ProcessingException {
        try {
            return getTicketFromJira(context.projectName(), context.allVersions());
        } catch (IOException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private ReturnTickets getTicketFromJira(String projectName, List<Version> allVersions) throws IOException {
        int i =  0;
        int j;
        int total;
        String url;
        List<Ticket> tickets = new ArrayList<>();
        List<Ticket> toBeCorrected = new ArrayList<>();
        do {
            j = i + 1000;
            url = formatUrl(projectName, i, j);
            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray(ISSUES);
            total = json.getInt(TOTAL);
            // Iterate through each bug
            for (; i < total && i < j; i++) {
                // Extract affectedVersions from json
                JSONArray jsonAffectedVersions = issues.getJSONObject(i % 1000).getJSONObject(FIELDS).getJSONArray(AFFECTED_VERSIONS);
                List<Version> affectedVersions = extractVersions(jsonAffectedVersions, allVersions);

                // Get key and date from json
                String key = issues.getJSONObject(i % 1000).getString(KEY);
                String openingDate = issues.getJSONObject(i % 1000).getJSONObject(FIELDS).getString(CREATED);

                // Get resolution date and correspondent fixedVersion
                String resolutionDate = issues.getJSONObject(i % 1000).getJSONObject(FIELDS).getString(RESOLUTION_DATE);
                Version fixedVersion = getVersionByDate(resolutionDate, allVersions);

                // Before use first affected versions as injected version, check if the first affected is before then opening version
                Version openingVersion = getVersionByDate(openingDate, allVersions);

                manageTickets(key, openingVersion, affectedVersions, fixedVersion, toBeCorrected, tickets);
            }
        } while (i < total);
        tickets.sort(Comparator.comparing(ticket -> ticket.getFixedVersion().getReleaseDate()));
        return new ReturnTickets(tickets, toBeCorrected);
    }

    private void manageTickets(String ticketKey, Version openingVersion, List<Version> affectedVersions, Version fixedVersion, List<Ticket> toBeCorrected, List<Ticket> tickets) {
        // If ticket has FV, AV, OV
        if (fixedVersion != null && openingVersion != null) {

            // OV < FV
            boolean isOpenBeforeFix = versionIsBefore(openingVersion, fixedVersion);

            if (!affectedVersions.isEmpty()) {
                // Candidate for IV is the first AV
                Version injectedVersion = affectedVersions.getFirst();

                // IV <= OV
                boolean isInjectedBeforeOpen = versionIsBeforeOrEqual(injectedVersion, openingVersion);

                if (isOpenBeforeFix && isInjectedBeforeOpen) {
                    Ticket ticket = new Ticket(ticketKey, affectedVersions, fixedVersion, injectedVersion, openingVersion);
                    tickets.add(ticket);
                }
            } else if (isOpenBeforeFix) {
                Ticket ticket = new Ticket(ticketKey, affectedVersions, fixedVersion, null, openingVersion);
                toBeCorrected.add(ticket);
            }
        }
    }

    private List<Version> extractVersions(JSONArray jsonVersions, List<Version> allVersions) {
        List<Version> versions = new ArrayList<>();
        for (int i = 0; i < jsonVersions.length(); i++) {
            JSONObject current  = jsonVersions.getJSONObject(i);
            // Consider only the released fixVersions
            if (!current.has(RELEASE_DATE) || !current.has(NAME)) continue;
            for (Version version : allVersions) {
                if (version.getReleaseDate().equals(current.getString(RELEASE_DATE)) && version.getName().equals(current.getString(NAME))) {
                    versions.add(version);
                }
            }
        }
        return versions;
    }

    private Version getVersionByDate(String date, List<Version> allVersions) {
        LocalDate openingLocalDate = string2LocalDate(date);
        for (Version version : allVersions) {
            LocalDate releaseDate = LocalDate.parse(version.getReleaseDate());

            // If the passed date is before or equals to the release data, the tickets "belongs" this version
            if (openingLocalDate.isBefore(releaseDate) || openingLocalDate.isEqual(releaseDate)) {
                return version;
            }
        }
        // Date is after the most recent version released
        return null;
    }

    private boolean versionIsBeforeOrEqual(Version v1, Version v2) {
        LocalDate v1Date = LocalDate.parse(v1.getReleaseDate());
        LocalDate v2Date = LocalDate.parse(v2.getReleaseDate());

        return versionIsBefore(v1, v2) || v1Date.isEqual(v2Date);
    }

    private boolean versionIsBefore(Version v1, Version v2) {
        LocalDate v1Date = LocalDate.parse(v1.getReleaseDate());
        LocalDate v2Date = LocalDate.parse(v2.getReleaseDate());
        return v1Date.isBefore(v2Date);
    }

    private String formatUrl(String projectName, Integer i, Integer j) {
        return "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                + projectName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                + i.toString() + "&maxResults=" + j.toString();
    }

    private LocalDate string2LocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        OffsetDateTime odt = OffsetDateTime.parse(date, formatter);
        return odt.toLocalDate();
    }

}
