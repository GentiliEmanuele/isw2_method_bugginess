package org.isw2.git.model;

import java.util.List;

public record Commit(String id, Author author, String commitTime, String message, List<Change> changes) {
}
