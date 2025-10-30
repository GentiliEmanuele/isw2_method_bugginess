package org.isw2.changes.model;

import java.util.ArrayList;
import java.util.List;

public class Version {
    private String id;
    private String name;
    private String releaseDate;
    private String description;
    private final List<Commit> commits;

    public Version() {
        this.commits = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    @Override
    public String toString() {
        return "Version{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", description='" + description + '\'' +
                ", commits=" + commits.toString() +
                '}';
    }
}
