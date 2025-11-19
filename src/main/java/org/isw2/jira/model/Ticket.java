package org.isw2.jira.model;

import java.util.List;

public class Ticket {
    private final String id;
    private List<Version> affectedVersions;
    private final Version fixedVersion;
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

    public void setAffectedVersions(List<Version> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    public Version getFixedVersion() {
        return fixedVersion;
    }

    public Version getInjectedVersion() {
        return injectedVersion;
    }

    public void setInjectedVersion(Version injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public Version getOpeningVersion() {
        return openingVersion;
    }

    public void setOpeningVersion(Version openingVersion) {
        this.openingVersion = openingVersion;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id='" + id + '\'' +
                ", affectedVersions=" + affectedVersions +
                ", fixedVersion=" + fixedVersion +
                ", injectedVersion=" + injectedVersion +
                ", openingVersion=" + openingVersion +
                '}';
    }
}
