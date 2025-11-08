package org.isw2.changes.controller;

import org.isw2.changes.model.Commit;
import org.isw2.jira.model.Version;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MergeVersionAndCommit {
    public void mergeVersionAndCommit(List<Version> versions, List<Commit> commits) {
        int commitSize = commits.size();
        AtomicInteger processed = new AtomicInteger();
        // Sort versions list using the releaseDate
        versions.sort(Comparator.comparing(v -> LocalDate.parse(v.getReleaseDate(), DateTimeFormatter.ISO_LOCAL_DATE)));
        // Sort commits list using the commitTime
        commits.sort(Comparator.comparing(c -> LocalDate.parse(c.getCommitTime(), DateTimeFormatter.ISO_LOCAL_DATE)));
        // Associate commits and versions
        commits.forEach(c -> {
            for (Version v : versions) {
                if (LocalDate.parse(c.getCommitTime(), DateTimeFormatter.ISO_LOCAL_DATE).isBefore(LocalDate.parse(v.getReleaseDate(), DateTimeFormatter.ISO_LOCAL_DATE)) ||
                    LocalDate.parse(c.getCommitTime(), DateTimeFormatter.ISO_LOCAL_DATE).isEqual(LocalDate.parse(v.getReleaseDate(), DateTimeFormatter.ISO_LOCAL_DATE))) {
                    v.getCommits().add(c);
                    break;
                }
            }
            processed.getAndIncrement();
            int percent = (100 * processed.get()) / commitSize;
            System.out.print("\rMerge version and commit progress: " + percent + "%");
        });
        System.out.println();
        versions.removeIf(v -> v.getCommits().isEmpty());
    }
}
