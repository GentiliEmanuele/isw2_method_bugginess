package org.isw2.jira.model;

import org.isw2.git.model.Commit;

import java.util.List;

public class Ticket {
    private final String key;
    private List<Version> affectedVersions;
    private final Version fixedVersion;
    private Version injectedVersion;
    private Version openingVersion;
    private List<Commit> fixedCommits;

    public Ticket(String id, List<Version> affectedVersions, Version fixedVersion, Version injectedVersion, Version openingVersion) {
        this.key = id;
        this.affectedVersions = affectedVersions;
        this.fixedVersion = fixedVersion;
        this.injectedVersion = injectedVersion;
        this.openingVersion = openingVersion;
    }

    public String getKey() {
        return key;
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

    public List<Commit> getFixedCommits() {
        return fixedCommits;
    }

    public void setFixedCommits(List<Commit> fixedCommits) {
        this.fixedCommits = fixedCommits;
    }
}
