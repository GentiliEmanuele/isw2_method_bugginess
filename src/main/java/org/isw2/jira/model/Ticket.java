package org.isw2.jira.model;

import java.util.List;

public class Ticket {
    private String id;
    private List<Version> affectedVersions;
    private Version fixedVersion;
    private Version injectedVersion;
    private Version openingVersion;

    public Ticket(String id, List<Version> affectedVersions, Version fixedVersion, Version injectedVersion, Version openingVersion) {
        this.id = id;
        this.affectedVersions = affectedVersions;
        this.fixedVersion = fixedVersion;
        this.injectedVersion = injectedVersion;
        this.openingVersion = openingVersion;
    }

    public String getId() {
        return id;
    }

    public List<Version> getAffectedVersions() {
        return affectedVersions;
    }

    public Version getFixedVersion() {
        return fixedVersion;
    }

    public Version getInjectedVersion() {
        return injectedVersion;
    }

    public Version getOpeningVersion() {
        return openingVersion;
    }

}
