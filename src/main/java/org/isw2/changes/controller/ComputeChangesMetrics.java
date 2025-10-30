package org.isw2.changes.controller;

import org.isw2.changes.model.Author;
import org.isw2.changes.model.Commit;
import org.isw2.changes.model.Version;
import org.isw2.complexity.model.Method;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComputeChangesMetrics {

    private LocalDate stringAsLocalDate(String s) {
        return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public long computeMethodHistories(Method method, Version start, Version end) {
        List<Commit> commits = method.getTouchedBy();
        LocalDate startDate = stringAsLocalDate(start.getReleaseDate());
        LocalDate endDate = stringAsLocalDate(end.getReleaseDate());
        if (!commits.isEmpty()) {
            if (startDate.equals(endDate)) {
                return commits.stream().filter(c -> {
                    LocalDate commitTime = stringAsLocalDate(c.getCommitTime());
                    return commitTime.isBefore(endDate) || commitTime.isEqual(endDate);
                }).count();
            } else {
                return commits.stream().filter(c -> {
                    LocalDate commitTime = stringAsLocalDate(c.getCommitTime());
                    return (commitTime.isAfter(startDate) || commitTime.isEqual(startDate)) &&
                            (commitTime.isBefore(endDate) || commitTime.isEqual(endDate));
                }).count();
            }
        } else return 0;
    }

    public int computeAuthors(Method method, Version start, Version end) {
        List<Commit> commits = method.getTouchedBy();
        Set<Author> authors = new HashSet<>();
        LocalDate startDate = stringAsLocalDate(start.getReleaseDate());
        LocalDate endDate = stringAsLocalDate(end.getReleaseDate());
        if (!commits.isEmpty()) {
            if (startDate.equals(endDate)) {
                for (Commit c : commits) {
                    LocalDate commitTime = stringAsLocalDate(c.getCommitTime());
                    if (commitTime.isBefore(endDate) || commitTime.isEqual(endDate)) {
                        authors.add(c.getAuthor());
                    }
                }
            } else {
                for (Commit c : commits) {
                    LocalDate commitTime = stringAsLocalDate(c.getCommitTime());
                    if ((commitTime.isAfter(startDate) || commitTime.isEqual(startDate))
                            && (commitTime.isBefore(endDate) || commitTime.isEqual(endDate))) {
                        authors.add(c.getAuthor());
                    }
                }
            }
        }
        return authors.size();
    }
}
