package org.isw2.dataset.git.model;

import java.util.List;

public record Commit(String id, Author author, String commitTime, String message, List<Change> changes) {
}
