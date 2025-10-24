package org.isw2.changes.controller;

import org.isw2.changes.model.Commit;
import org.isw2.changes.model.Version;
import org.isw2.complexity.model.Method;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ControllerChangesMetrics {

    private LocalDate stringAsLocalDate(String s) {
        return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public long computeMethodHistories(Method method, Version start, Version end) {
        List<Commit> commits = method.getTouchedBy();
        if (!commits.isEmpty()) {
            return commits.stream().filter(c -> {
                LocalDate commitTime = stringAsLocalDate(c.getCommitTime());
                return commitTime.isAfter(stringAsLocalDate(start.getReleaseDate())) && commitTime.isBefore(stringAsLocalDate(end.getReleaseDate()));
            }).count();
        } else return 0;
    }

}
